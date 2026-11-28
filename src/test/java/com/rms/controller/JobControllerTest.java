package com.rms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rms.config.SecurityConfig;
import com.rms.dto.JobRequestDto;
import com.rms.dto.JobResponseDto;
import com.rms.exception.GlobalExceptionHandler;
import com.rms.exception.ResourceNotFoundException;
import com.rms.service.JobService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = JobController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobService jobService;

    @Test
    @WithMockUser
    @DisplayName("GET /api/jobs/active - Authenticated users can list active vacancies")
    void shouldGetActiveJobs() throws Exception {
        when(jobService.getActiveJobs()).thenReturn(List.of(response(true)));

        mockMvc.perform(get("/api/jobs/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Backend Engineer"));
    }

    @Test
    @DisplayName("GET /api/jobs/{id} - Anonymous users get 401")
    void shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("GET /api/jobs/{id} - Should return job details")
    void shouldGetJobById() throws Exception {
        when(jobService.getJobById(1L)).thenReturn(response(true));

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Backend Engineer"));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("GET /api/jobs/{id} - Missing vacancy returns 404")
    void shouldReturn404ForMissingJob() throws Exception {
        when(jobService.getJobById(9L)).thenThrow(new ResourceNotFoundException("Job vacancy not found"));

        mockMvc.perform(get("/api/jobs/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    @DisplayName("POST /api/jobs - Recruiter can create a vacancy and gets 201 with its location")
    void shouldCreateJob() throws Exception {
        when(jobService.createJob(any(JobRequestDto.class))).thenReturn(response(true));

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest().build())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/jobs/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("POST /api/jobs - Candidate is forbidden")
    void shouldForbidCandidateCreatingJob() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest().build())))
                .andExpect(status().isForbidden());

        verifyNoInteractions(jobService);
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    @DisplayName("POST /api/jobs - Blank title and blank skill return 400")
    void shouldRejectInvalidFields() throws Exception {
        JobRequestDto request = validRequest().title(" ").requiredSkills(java.util.Set.of(" ")).build();

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());

        verifyNoInteractions(jobService);
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    @DisplayName("POST /api/jobs - Maximum salary below minimum returns 400")
    void shouldRejectInvertedSalaryRange() throws Exception {
        JobRequestDto request = validRequest()
                .salaryMin(new BigDecimal("900"))
                .salaryMax(new BigDecimal("100"))
                .build();

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.salaryRangeValid").exists());
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    @DisplayName("POST /api/jobs - Malformed JSON returns 400")
    void shouldRejectMalformedBody() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/jobs/{id} - Admin can edit a vacancy")
    void shouldUpdateJob() throws Exception {
        when(jobService.updateJob(eq(1L), any(JobRequestDto.class))).thenReturn(response(true));

        mockMvc.perform(put("/api/jobs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest().build())))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    @DisplayName("DELETE /api/jobs/{id} - Deleted vacancy returns 204")
    void shouldReturn204WhenDeleted() throws Exception {
        when(jobService.deleteJob(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/jobs/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    @DisplayName("DELETE /api/jobs/{id} - Vacancy with applications is returned as closed")
    void shouldReturnClosedJobWhenItHasApplications() throws Exception {
        when(jobService.deleteJob(1L)).thenReturn(Optional.of(response(false)));

        mockMvc.perform(delete("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("DELETE /api/jobs/{id} - Candidate is forbidden")
    void shouldForbidCandidateDeletingJob() throws Exception {
        mockMvc.perform(delete("/api/jobs/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(jobService);
    }

    private String json(JobRequestDto request) throws Exception {
        return objectMapper.writeValueAsString(request);
    }

    private JobRequestDto.JobRequestDtoBuilder validRequest() {
        return JobRequestDto.builder()
                .title("Backend Engineer")
                .department("Engineering")
                .location("Khulna")
                .employmentType("Full-Time")
                .description("Build REST APIs.");
    }

    private JobResponseDto response(boolean active) {
        return JobResponseDto.builder().id(1L).title("Backend Engineer").active(active).build();
    }
}
