package com.luxury.core.api.service;

import com.luxury.core.api.dto.GenericEntityResponse;
import com.luxury.core.api.exception.DynamicValidationException;
import com.luxury.core.api.repository.GenericEntityRepository;
import com.luxury.core.api.validation.DynamicPayloadValidator;
import com.luxury.core.persistence.repository.SysDbObjectRepository;
import com.luxury.core.security.service.SecurityContextService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GenericEntityService}.
 *
 * <p>Verifies entity existence checks, delegation to validator and repository,
 * audit field population, and error handling.</p>
 */
@ExtendWith(MockitoExtension.class)
class GenericEntityServiceTest {

    @Mock
    private SysDbObjectRepository sysDbObjectRepository;

    @Mock
    private GenericEntityRepository genericEntityRepository;

    @Mock
    private DynamicPayloadValidator payloadValidator;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private GenericEntityService service;

    private static final String ENTITY = "hyper_profiles";
    private static final UUID RECORD_ID = UUID.randomUUID();
    private static final String USERNAME = "concierge@luxury.com";

    private Map<String, Object> sampleRow() {
        Map<String, Object> row = new HashMap<>();
        row.put("id", RECORD_ID);
        row.put("first_name", "Arabella");
        row.put("last_name", "Ashton-Whitley");
        row.put("created_at", Timestamp.from(Instant.now()));
        row.put("updated_at", Timestamp.from(Instant.now()));
        row.put("created_by", USERNAME);
        row.put("updated_by", USERNAME);
        row.put("version", 0L);
        row.put("is_deleted", false);
        return row;
    }

    private void stubEntityExists() {
        when(sysDbObjectRepository.existsByName(ENTITY)).thenReturn(true);
        when(genericEntityRepository.tableExists(ENTITY)).thenReturn(true);
    }

    @Nested
    @DisplayName("Entity Existence Validation")
    class EntityExistenceTests {

        @Test
        @DisplayName("Throws EntityNotFoundException for unregistered entity")
        void throwsForUnregisteredEntity() {
            when(sysDbObjectRepository.existsByName("phantom_entity")).thenReturn(false);

            assertThatThrownBy(() -> service.getById("phantom_entity", RECORD_ID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("not registered");
        }

        @Test
        @DisplayName("Throws IllegalStateException when physical table missing")
        void throwsForMissingTable() {
            when(sysDbObjectRepository.existsByName(ENTITY)).thenReturn(true);
            when(genericEntityRepository.tableExists(ENTITY)).thenReturn(false);

            assertThatThrownBy(() -> service.getById(ENTITY, RECORD_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("physical table does not exist");
        }
    }

    @Nested
    @DisplayName("List Operations")
    class ListTests {

        @Test
        @DisplayName("Returns paginated results from repository")
        void returnsPaginatedResults() {
            stubEntityExists();
            Pageable pageable = PageRequest.of(0, 20);
            Page<Map<String, Object>> page = new PageImpl<>(List.of(sampleRow()), pageable, 1);
            when(genericEntityRepository.findAll(ENTITY, pageable)).thenReturn(page);

            Page<GenericEntityResponse> result = service.list(ENTITY, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getEntityName()).isEqualTo(ENTITY);
            assertThat(result.getContent().getFirst().getId()).isEqualTo(RECORD_ID);
        }
    }

    @Nested
    @DisplayName("Get By ID")
    class GetByIdTests {

        @Test
        @DisplayName("Returns entity response for existing record")
        void returnsForExistingRecord() {
            stubEntityExists();
            when(genericEntityRepository.findById(ENTITY, RECORD_ID))
                    .thenReturn(Optional.of(sampleRow()));

            GenericEntityResponse response = service.getById(ENTITY, RECORD_ID);

            assertThat(response.getId()).isEqualTo(RECORD_ID);
            assertThat(response.getEntityName()).isEqualTo(ENTITY);
            assertThat(response.getAttributes()).containsEntry("first_name", "Arabella");
            // System fields stripped from attributes
            assertThat(response.getAttributes()).doesNotContainKey("version");
            assertThat(response.getAttributes()).doesNotContainKey("is_deleted");
        }

        @Test
        @DisplayName("Throws EntityNotFoundException for missing record")
        void throwsForMissingRecord() {
            stubEntityExists();
            when(genericEntityRepository.findById(ENTITY, RECORD_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getById(ENTITY, RECORD_ID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(RECORD_ID.toString());
        }
    }

    @Nested
    @DisplayName("Create Operations")
    class CreateTests {

        @Test
        @DisplayName("Delegates to validator and repository on valid payload")
        void delegatesOnValidPayload() {
            stubEntityExists();
            Map<String, Object> attrs = Map.of("first_name", "Arabella", "last_name", "Ashton-Whitley");

            when(payloadValidator.validate(ENTITY, attrs, false)).thenReturn(List.of());
            when(securityContextService.getCurrentUsername()).thenReturn(USERNAME);
            when(genericEntityRepository.insert(eq(ENTITY), eq(attrs), eq(USERNAME)))
                    .thenReturn(sampleRow());

            GenericEntityResponse response = service.create(ENTITY, attrs);

            assertThat(response.getId()).isEqualTo(RECORD_ID);
            verify(payloadValidator).validate(ENTITY, attrs, false);
            verify(genericEntityRepository).insert(ENTITY, attrs, USERNAME);
        }

        @Test
        @DisplayName("Throws DynamicValidationException on invalid payload")
        void throwsOnInvalidPayload() {
            stubEntityExists();
            Map<String, Object> attrs = Map.of("bogus", "field");

            when(payloadValidator.validate(ENTITY, attrs, false))
                    .thenReturn(List.of("Unknown field 'bogus'"));

            assertThatThrownBy(() -> service.create(ENTITY, attrs))
                    .isInstanceOf(DynamicValidationException.class);

            verify(genericEntityRepository, never()).insert(anyString(), any(), anyString());
        }

        @Test
        @DisplayName("Sets audit username from SecurityContextService")
        void setsAuditUsername() {
            stubEntityExists();
            Map<String, Object> attrs = Map.of("first_name", "Test");

            when(payloadValidator.validate(ENTITY, attrs, false)).thenReturn(List.of());
            when(securityContextService.getCurrentUsername()).thenReturn("vip@luxury.com");
            when(genericEntityRepository.insert(eq(ENTITY), eq(attrs), eq("vip@luxury.com")))
                    .thenReturn(sampleRow());

            service.create(ENTITY, attrs);

            verify(securityContextService).getCurrentUsername();
            verify(genericEntityRepository).insert(ENTITY, attrs, "vip@luxury.com");
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateTests {

        @Test
        @DisplayName("Uses partial update mode for validation")
        void usesPartialUpdateMode() {
            stubEntityExists();
            Map<String, Object> attrs = Map.of("first_name", "Updated");

            when(payloadValidator.validate(ENTITY, attrs, true)).thenReturn(List.of());
            when(securityContextService.getCurrentUsername()).thenReturn(USERNAME);
            when(genericEntityRepository.update(eq(ENTITY), eq(RECORD_ID), eq(attrs), eq(USERNAME)))
                    .thenReturn(Optional.of(sampleRow()));

            service.update(ENTITY, RECORD_ID, attrs);

            verify(payloadValidator).validate(ENTITY, attrs, true);
        }

        @Test
        @DisplayName("Throws EntityNotFoundException when record not found during update")
        void throwsOnMissingRecordUpdate() {
            stubEntityExists();
            Map<String, Object> attrs = Map.of("first_name", "Updated");

            when(payloadValidator.validate(ENTITY, attrs, true)).thenReturn(List.of());
            when(securityContextService.getCurrentUsername()).thenReturn(USERNAME);
            when(genericEntityRepository.update(eq(ENTITY), eq(RECORD_ID), eq(attrs), eq(USERNAME)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(ENTITY, RECORD_ID, attrs))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteTests {

        @Test
        @DisplayName("Delegates soft-delete to repository")
        void delegatesSoftDelete() {
            stubEntityExists();
            when(securityContextService.getCurrentUsername()).thenReturn(USERNAME);
            when(genericEntityRepository.softDelete(ENTITY, RECORD_ID, USERNAME)).thenReturn(true);

            service.delete(ENTITY, RECORD_ID);

            verify(genericEntityRepository).softDelete(ENTITY, RECORD_ID, USERNAME);
        }

        @Test
        @DisplayName("Throws EntityNotFoundException when record not found for delete")
        void throwsOnMissingRecordDelete() {
            stubEntityExists();
            when(securityContextService.getCurrentUsername()).thenReturn(USERNAME);
            when(genericEntityRepository.softDelete(ENTITY, RECORD_ID, USERNAME)).thenReturn(false);

            assertThatThrownBy(() -> service.delete(ENTITY, RECORD_ID))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
