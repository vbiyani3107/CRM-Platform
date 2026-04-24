package com.luxury.core.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxury.common.exception.GlobalExceptionHandler;
import com.luxury.core.api.dto.GenericEntityRequest;
import com.luxury.core.api.dto.GenericEntityResponse;
import com.luxury.core.api.exception.DynamicValidationException;
import com.luxury.core.api.service.GenericEntityService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link GenericEntityController}.
 *
 * <p>Uses standalone MockMvc setup to test HTTP behavior, request/response
 * mapping, and error handling without loading the full application context.</p>
 */
@WebMvcTest(
        controllers = GenericEntityController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.luxury\\.core\\.security\\..*"
        )
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class GenericEntityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GenericEntityService genericEntityService;

    private static final String ENTITY = "hyper_profiles";
    private static final UUID RECORD_ID = UUID.randomUUID();
    private static final String BASE_URL = "/v1/entities";

    private GenericEntityResponse sampleResponse() {
        return GenericEntityResponse.builder()
                .id(RECORD_ID)
                .entityName(ENTITY)
                .attributes(Map.of("first_name", "Arabella", "last_name", "Ashton-Whitley"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy("test@luxury.com")
                .updatedBy("test@luxury.com")
                .build();
    }

    @Nested
    @DisplayName("GET /entities/{entityName}")
    class ListEndpointTests {

        @Test
        @DisplayName("Returns paginated list in ApiResponse wrapper")
        void returnsPaginatedList() throws Exception {
            Pageable pageable = PageRequest.of(0, 20);
            when(genericEntityService.list(eq(ENTITY), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(sampleResponse()), pageable, 1));

            mockMvc.perform(get(BASE_URL + "/" + ENTITY))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].id").value(RECORD_ID.toString()))
                    .andExpect(jsonPath("$.data.content[0].entity_name").value(ENTITY));
        }

        @Test
        @DisplayName("Returns 404 for unregistered entity")
        void returns404ForUnregisteredEntity() throws Exception {
            when(genericEntityService.list(eq("phantom"), any(Pageable.class)))
                    .thenThrow(new EntityNotFoundException("Entity 'phantom' is not registered"));

            mockMvc.perform(get(BASE_URL + "/phantom"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.status").value(404));
        }
    }

    @Nested
    @DisplayName("GET /entities/{entityName}/{id}")
    class GetByIdEndpointTests {

        @Test
        @DisplayName("Returns single entity in ApiResponse")
        void returnsSingleEntity() throws Exception {
            when(genericEntityService.getById(ENTITY, RECORD_ID)).thenReturn(sampleResponse());

            mockMvc.perform(get(BASE_URL + "/" + ENTITY + "/" + RECORD_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(RECORD_ID.toString()))
                    .andExpect(jsonPath("$.data.attributes.first_name").value("Arabella"));
        }

        @Test
        @DisplayName("Returns 404 for nonexistent record")
        void returns404ForMissingRecord() throws Exception {
            UUID missing = UUID.randomUUID();
            when(genericEntityService.getById(ENTITY, missing))
                    .thenThrow(new EntityNotFoundException("Record not found"));

            mockMvc.perform(get(BASE_URL + "/" + ENTITY + "/" + missing))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.title").value("Resource Not Found"));
        }
    }

    @Nested
    @DisplayName("POST /entities/{entityName}")
    class CreateEndpointTests {

        @Test
        @DisplayName("Returns 201 with created entity on valid payload")
        void returns201OnValid() throws Exception {
            Map<String, Object> attrs = Map.of("first_name", "Arabella", "last_name", "Ashton-Whitley");
            when(genericEntityService.create(eq(ENTITY), any())).thenReturn(sampleResponse());

            mockMvc.perform(post(BASE_URL + "/" + ENTITY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    GenericEntityRequest.builder().attributes(attrs).build())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(RECORD_ID.toString()));
        }

        @Test
        @DisplayName("Returns 400 with RFC 7807 on validation failure")
        void returns400OnValidationFailure() throws Exception {
            Map<String, Object> attrs = Map.of("bogus", "field");
            when(genericEntityService.create(eq(ENTITY), any()))
                    .thenThrow(new DynamicValidationException(List.of(
                            "Unknown field 'bogus'",
                            "Required field 'first_name' is missing"
                    )));

            mockMvc.perform(post(BASE_URL + "/" + ENTITY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    GenericEntityRequest.builder().attributes(attrs).build())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.type").value("https://luxury.com/errors/dynamic-validation"))
                    .andExpect(jsonPath("$.error.title").value("Dynamic Validation Error"))
                    .andExpect(jsonPath("$.error.detail").exists());
        }

        @Test
        @DisplayName("Returns 400 when attributes map is empty")
        void returns400OnEmptyAttributes() throws Exception {
            mockMvc.perform(post(BASE_URL + "/" + ENTITY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"attributes\":{}}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /entities/{entityName}/{id}")
    class UpdateEndpointTests {

        @Test
        @DisplayName("Returns 200 with updated entity")
        void returns200OnSuccess() throws Exception {
            Map<String, Object> attrs = Map.of("first_name", "Updated");
            when(genericEntityService.update(eq(ENTITY), eq(RECORD_ID), any()))
                    .thenReturn(sampleResponse());

            mockMvc.perform(put(BASE_URL + "/" + ENTITY + "/" + RECORD_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    GenericEntityRequest.builder().attributes(attrs).build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /entities/{entityName}/{id}")
    class DeleteEndpointTests {

        @Test
        @DisplayName("Returns 204 on successful delete")
        void returns204OnSuccess() throws Exception {
            doNothing().when(genericEntityService).delete(ENTITY, RECORD_ID);

            mockMvc.perform(delete(BASE_URL + "/" + ENTITY + "/" + RECORD_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Returns 404 when record not found for delete")
        void returns404OnMissingRecord() throws Exception {
            doThrow(new EntityNotFoundException("Not found"))
                    .when(genericEntityService).delete(ENTITY, RECORD_ID);

            mockMvc.perform(delete(BASE_URL + "/" + ENTITY + "/" + RECORD_ID))
                    .andExpect(status().isNotFound());
        }
    }
}
