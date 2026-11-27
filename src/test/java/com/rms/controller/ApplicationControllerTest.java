package com.rms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rms.config.SecurityConfig;
import com.rms.dto.ApplicationApplyRequestDto;
import com.rms.dto.ApplicationResponseDto;
import com.rms.dto.ApplicationStatusUpdateRequestDto;
import com.rms.dto.JobResponseDto;
import com.rms.entity.ApplicationStatus;
import com.rms.exception.GlobalExceptionHandler;
import com.rms.service.ApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ApplicationController.class, JobController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationService applicationService;

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("POST /api/applications/apply - Candidate should apply and get 201 Created")
    void shouldApplyForJobSuccessfully() throws Exception {
        ApplicationApplyRequestDto request = ApplicationApplyRequestDto.builder()
                .jobId(10L)
                .coverLetter("Excited about this opportunity.")
                .build();

        ApplicationResponseDto response = ApplicationResponseDto.builder()
                .id(100L)
                .jobId(10L)
                .jobTitle("Senior Java Engineer")
                .status(ApplicationStatus.SUBMITTED)
                .appliedAt(LocalDateTime.now())
                .build();

        when(applicationService.applyForJob(any(ApplicationApplyRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/applications/apply")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.jobTitle").value("Senior Java Engineer"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    @DisplayName("POST /api/applications/apply - Recruiter should be denied (403 Forbidden)")
    void shouldDenyRecruiterApplyingForJob() throws Exception {
        ApplicationApplyRequestDto request = ApplicationApplyRequestDto.builder()
                .jobId(10L)
                .build();

        mockMvc.perform(post("/api/applications/apply")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("GET /api/applications/my-applications - Candidate should get their applications list")
    void shouldGetMyApplications() throws Exception {
        ApplicationResponseDto app = ApplicationResponseDto.builder()
                .id(100L)
                .jobTitle("Cloud Architect")
                .status(ApplicationStatus.SUBMITTED)
                .build();

        when(applicationService.getMyApplications()).thenReturn(List.of(app));

        mockMvc.perform(get("/api/applications/my-applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[0].jobTitle").value("Cloud Architect"));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("PUT /api/applications/{id}/withdraw - Candidate can withdraw application")
    void shouldWithdrawApplication() throws Exception {
        ApplicationResponseDto response = ApplicationResponseDto.builder()
                .id(100L)
                .status(ApplicationStatus.WITHDRAWN)
                .build();

        when(applicationService.withdrawApplication(100L)).thenReturn(response);

        mockMvc.perform(put("/api/applications/100/withdraw")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WITHDRAWN"));
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    @DisplayName("PUT /api/applications/{id}/status - Recruiter can update application status")
    void shouldUpdateApplicationStatusWhenRecruiter() throws Exception {
        ApplicationStatusUpdateRequestDto request = ApplicationStatusUpdateRequestDto.builder()
                .status(ApplicationStatus.SHORTLISTED)
                .recruiterNotes("Promising background.")
                .build();

        ApplicationResponseDto response = ApplicationResponseDto.builder()
                .id(100L)
                .status(ApplicationStatus.SHORTLISTED)
                .recruiterNotes("Promising background.")
                .build();

        when(applicationService.updateApplicationStatus(eq(100L), any(ApplicationStatusUpdateRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/applications/100/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHORTLISTED"))
                .andExpect(jsonPath("$.recruiterNotes").value("Promising background."));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("PUT /api/applications/{id}/status - Candidate is forbidden from changing status")
    void shouldDenyCandidateUpdatingApplicationStatus() throws Exception {
        ApplicationStatusUpdateRequestDto request = ApplicationStatusUpdateRequestDto.builder()
                .status(ApplicationStatus.HIRED)
                .build();

        mockMvc.perform(put("/api/applications/100/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    @DisplayName("GET /api/applications/job/{jobId} - Recruiter can get applications for a job")
    void shouldGetApplicationsByJobWhenRecruiter() throws Exception {
        ApplicationResponseDto app = ApplicationResponseDto.builder()
                .id(100L)
                .candidateUsername("sarah_c")
                .status(ApplicationStatus.SUBMITTED)
                .build();

        when(applicationService.getApplicationsByJob(10L)).thenReturn(List.of(app));

        mockMvc.perform(get("/api/applications/job/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].candidateUsername").value("sarah_c"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/jobs/active - Authenticated users can list active vacancies")
    void shouldGetActiveJobs() throws Exception {
        JobResponseDto job = JobResponseDto.builder()
                .id(10L)
                .title("Senior Java Engineer")
                .department("Engineering")
                .build();

        when(applicationService.getActiveJobs()).thenReturn(List.of(job));

        mockMvc.perform(get("/api/jobs/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Senior Java Engineer"));
    }
}
