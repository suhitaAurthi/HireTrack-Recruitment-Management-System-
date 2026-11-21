package com.rms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rms.config.SecurityConfig;
import com.rms.dto.CandidateProfileRequestDto;
import com.rms.dto.CandidateProfileResponseDto;
import com.rms.exception.GlobalExceptionHandler;
import com.rms.service.CandidateProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CandidateProfileController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class CandidateProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CandidateProfileService candidateProfileService;

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("POST /api/candidate/profile - Should update profile and return 200 OK")
    void shouldCreateOrUpdateProfile() throws Exception {
        CandidateProfileRequestDto request = CandidateProfileRequestDto.builder()
                .headline("Senior Java Engineer")
                .summary("5 years of backend engineering")
                .phone("+8801700000000")
                .location("Dhaka")
                .skills(Set.of("Java", "Spring Boot", "Docker"))
                .build();

        CandidateProfileResponseDto response = CandidateProfileResponseDto.builder()
                .id(1L)
                .username("candidate1")
                .headline("Senior Java Engineer")
                .skills(Set.of("Java", "Spring Boot", "Docker"))
                .createdAt(LocalDateTime.now())
                .build();

        when(candidateProfileService.createOrUpdateProfile(any(CandidateProfileRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/candidate/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Senior Java Engineer"))
                .andExpect(jsonPath("$.username").value("candidate1"));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("GET /api/candidate/profile/me - Should return current user profile")
    void shouldGetMyProfile() throws Exception {
        CandidateProfileResponseDto response = CandidateProfileResponseDto.builder()
                .id(1L)
                .username("candidate1")
                .headline("Backend Engineer")
                .build();

        when(candidateProfileService.getMyProfile()).thenReturn(response);

        mockMvc.perform(get("/api/candidate/profile/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Backend Engineer"));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("POST /api/candidate/profile/resume - Should upload resume file and return 200 OK")
    void shouldUploadResume() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                "application/pdf",
                "Sample PDF content".getBytes()
        );

        CandidateProfileResponseDto response = CandidateProfileResponseDto.builder()
                .id(1L)
                .resumeFileName("cv.pdf")
                .resumeUrl("uploads/resumes/cv.pdf")
                .build();

        when(candidateProfileService.uploadResume(any())).thenReturn(response);

        mockMvc.perform(multipart("/api/candidate/profile/resume")
                .file(file)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumeFileName").value("cv.pdf"));
    }
}