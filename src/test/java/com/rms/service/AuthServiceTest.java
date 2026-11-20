package com.rms.service;

import com.rms.dto.LoginRequestDto;
import com.rms.dto.RegisterRequestDto;
import com.rms.dto.UserResponseDto;
import com.rms.entity.Role;
import com.rms.entity.RoleType;
import com.rms.entity.User;
import com.rms.exception.DuplicateResourceException;
import com.rms.repository.RoleRepository;
import com.rms.repository.UserRepository;
import com.rms.service.impl.AuthServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpServletResponse httpResponse;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequestDto requestDto;
    private Role candidateRole;

    @BeforeEach
    void setUp() {
        requestDto = RegisterRequestDto.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("plainPassword123")
                .firstName("John")
                .lastName("Doe")
                .build();

        candidateRole = Role.builder()
                .id(1L)
                .name(RoleType.ROLE_CANDIDATE)
                .build();
    }

    @Test
    @DisplayName("Should successfully register a candidate with BCrypt encrypted password")
    void shouldRegisterUserSuccessfully() {
        when(userRepository.existsByUsername(requestDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(roleRepository.findByName(RoleType.ROLE_CANDIDATE)).thenReturn(Optional.of(candidateRole));
        when(passwordEncoder.encode("plainPassword123")).thenReturn("$2a$10$encryptedHashValue");

        User savedUser = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .password("$2a$10$encryptedHashValue")
                .firstName("John")
                .lastName("Doe")
                .enabled(true)
                .roles(Set.of(candidateRole))
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDto response = authService.register(requestDto);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("john_doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getRoles()).contains("ROLE_CANDIDATE");

        verify(passwordEncoder, times(1)).encode("plainPassword123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when username is already taken")
    void shouldThrowExceptionWhenUsernameExists() {
        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(requestDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username 'john_doe' is already taken");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email is already registered")
    void shouldThrowExceptionWhenEmailExists() {
        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(requestDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email 'john@example.com' is already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should authenticate user and return profile on valid login")
    void shouldLoginSuccessfully() {
        LoginRequestDto loginRequest = new LoginRequestDto("john_doe", "password123");
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("john_doe");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        User user = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .roles(Set.of(candidateRole))
                .enabled(true)
                .build();
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(user));

        UserResponseDto response = authService.login(loginRequest, httpRequest, httpResponse);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("john_doe");
        assertThat(response.getRoles()).contains("ROLE_CANDIDATE");
    }
}