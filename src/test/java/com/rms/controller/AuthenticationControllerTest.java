package com.rms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rms.config.SecurityConfig;
import com.rms.dto.LoginRequestDto;
import com.rms.dto.RegisterRequestDto;
import com.rms.dto.UserResponseDto;
import com.rms.exception.DuplicateResourceException;
import com.rms.exception.GlobalExceptionHandler;
import com.rms.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthenticationController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/register - Should return 201 Created when request is valid")
    void shouldReturn201CreatedWhenValid() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("candidate_user")
                .email("candidate@rms.com")
                .password("securePass123")
                .firstName("Sarah")
                .lastName("Connor")
                .build();

        UserResponseDto response = UserResponseDto.builder()
                .id(10L)
                .username("candidate_user")
                .email("candidate@rms.com")
                .firstName("Sarah")
                .lastName("Connor")
                .enabled(true)
                .roles(Set.of("ROLE_CANDIDATE"))
                .createdAt(LocalDateTime.now())
                .build();

        when(authService.register(any(RegisterRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.username").value("candidate_user"))
                .andExpect(jsonPath("$.email").value("candidate@rms.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_CANDIDATE"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 Bad Request when validation fails")
    void shouldReturn400BadRequestWhenInvalid() throws Exception {
        RegisterRequestDto invalidRequest = RegisterRequestDto.builder()
                .username("")
                .email("not-an-email")
                .password("123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 409 Conflict when username already exists")
    void shouldReturn409ConflictWhenDuplicate() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("existing_user")
                .email("existing@rms.com")
                .password("validPassword123")
                .firstName("Jane")
                .lastName("Doe")
                .build();

        when(authService.register(any(RegisterRequestDto.class)))
                .thenThrow(new DuplicateResourceException("Username 'existing_user' is already taken"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Username 'existing_user' is already taken"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 200 OK with user details on successful login")
    void shouldReturn200OnSuccessfulLogin() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto("john_doe", "password123");
        UserResponseDto response = UserResponseDto.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .roles(Set.of("ROLE_CANDIDATE"))
                .enabled(true)
                .build();

        when(authService.login(any(LoginRequestDto.class), any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_CANDIDATE"));
    }

    @Test
    @DisplayName("POST /api/auth/logout - Should clear session and return 200 OK")
    void shouldReturn200OnLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }
}