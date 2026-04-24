package com.luxury.concierge.controller;

import com.luxury.concierge.dto.ExperientialRequestResponse;
import com.luxury.concierge.model.ExperientialRequest;
import com.luxury.concierge.service.ConciergeService;
import com.luxury.core.security.fls.FlsInterceptor;
import com.luxury.core.security.service.SecurityContextService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link ConciergeController}.
 *
 * <p>Uses @WebMvcTest with mocked service layer and security context.
 * Imports TestSecurityConfig to provide a mock JwtDecoder.</p>
 */
@WebMvcTest(ConciergeController.class)
@ActiveProfiles("test")
@Import(com.luxury.core.security.config.TestSecurityConfig.class)
@DisplayName("ConciergeController Unit Tests")
class ConciergeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConciergeService conciergeService;

    @MockBean
    private SecurityContextService securityContextService;

    @MockBean
    private FlsInterceptor flsInterceptor;

    private ExperientialRequestResponse buildResponse() {
        return ExperientialRequestResponse.builder()
                .id(UUID.randomUUID())
                .number("EXP0000001")
                .profileId(UUID.randomUUID())
                .requestType("Yacht Charter")
                .discretionLevel(ExperientialRequest.DiscretionLevel.High)
                .state(ExperientialRequest.RequestState.Open)
                .conciergeNotes("Mediterranean route")
                .targetDate(LocalDate.now().plusDays(30))
                .customAttributes(Map.of())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy("associate@luxury.com")
                .updatedBy("associate@luxury.com")
                .build();
    }

    @Test
    @WithMockUser(roles = "ASSOCIATE")
    @DisplayName("GET /requests should return paginated list")
    void listRequests() throws Exception {
        var resp = buildResponse();
        when(conciergeService.listRequests(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(resp)));

        mockMvc.perform(get("/v1/concierge/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].number").value("EXP0000001"));
    }

    @Test
    @WithMockUser(roles = "ASSOCIATE")
    @DisplayName("GET /requests/{id} should return request details")
    void getRequestById() throws Exception {
        UUID id = UUID.randomUUID();
        var resp = buildResponse();
        resp.setId(id);
        when(conciergeService.getRequestById(id)).thenReturn(resp);

        mockMvc.perform(get("/v1/concierge/requests/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.request_type").value("Yacht Charter"));
    }

    @Test
    @WithMockUser(roles = "ASSOCIATE")
    @DisplayName("POST /requests should create and return 201")
    void createRequest() throws Exception {
        var resp = buildResponse();
        when(conciergeService.createRequest(any())).thenReturn(resp);

        String body = """
                {
                    "profile_id": "%s",
                    "request_type": "Yacht Charter"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/v1/concierge/requests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ASSOCIATE")
    @DisplayName("PATCH /requests/{id} should update request")
    void updateRequest() throws Exception {
        UUID id = UUID.randomUUID();
        var resp = buildResponse();
        when(conciergeService.updateRequest(any(UUID.class), any())).thenReturn(resp);

        String body = """
                {
                    "concierge_notes": "Updated notes"
                }
                """;

        mockMvc.perform(patch("/v1/concierge/requests/" + id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ASSOCIATE")
    @DisplayName("POST /requests/{id}/fulfill should mark as fulfilled")
    void fulfillRequest() throws Exception {
        UUID id = UUID.randomUUID();
        var resp = buildResponse();
        resp.setState(ExperientialRequest.RequestState.Fulfilled);
        when(conciergeService.fulfillRequest(id)).thenReturn(resp);

        mockMvc.perform(post("/v1/concierge/requests/" + id + "/fulfill")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value("Fulfilled"));
    }

    @Test
    @DisplayName("Unauthenticated request should return 401")
    void unauthenticatedShouldReturn401() throws Exception {
        mockMvc.perform(get("/v1/concierge/requests"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ASSOCIATE")
    @DisplayName("GET /requests/{id} should return 404 for missing request")
    void shouldReturn404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(conciergeService.getRequestById(id))
                .thenThrow(new EntityNotFoundException("Not found"));

        mockMvc.perform(get("/v1/concierge/requests/" + id))
                .andExpect(status().isNotFound());
    }
}
