package com.rms.controller;

import com.rms.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RestController
class DummyProtectedController {
    @GetMapping("/api/admin/dashboard")
    public String admin() { return "admin"; }

    @GetMapping("/api/recruiter/jobs")
    public String recruiter() { return "recruiter"; }

    @GetMapping("/api/candidate/applications")
    public String candidate() { return "candidate"; }
}

@WebMvcTest(controllers = DummyProtectedController.class)
@Import(SecurityConfig.class)
class SecurityRoleAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Unauthenticated request to protected endpoint should return 401 Unauthorized")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("Candidate accessing /api/admin/** should return 403 Forbidden")
    void shouldReturn403WhenCandidateAccessesAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin accessing /api/admin/** should return 200 OK")
    void shouldReturn200WhenAdminAccessesAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    @DisplayName("Recruiter accessing /api/recruiter/** should return 200 OK")
    void shouldReturn200WhenRecruiterAccessesRecruiterEndpoint() throws Exception {
        mockMvc.perform(get("/api/recruiter/jobs"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("Candidate accessing /api/candidate/** should return 200 OK")
    void shouldReturn200WhenCandidateAccessesCandidateEndpoint() throws Exception {
        mockMvc.perform(get("/api/candidate/applications"))
                .andExpect(status().isOk());
    }
}