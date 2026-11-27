package com.rms.pattern.proxy;

import com.rms.dto.CandidateProfileResponseDto;
import com.rms.service.CandidateProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateProfileAccessProxyTest {

    @Mock
    private CandidateProfileService realService;

    @InjectMocks
    private CandidateProfileAccessProxy proxy;

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Proxy: Recruiter should be allowed to view any candidate profile")
    void shouldAllowRecruiterAccess() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("recruiter1", "pass", List.of(new SimpleGrantedAuthority("ROLE_RECRUITER")))
        );

        CandidateProfileResponseDto expected = CandidateProfileResponseDto.builder().id(99L).build();
        when(realService.getProfileById(99L)).thenReturn(expected);

        CandidateProfileResponseDto result = proxy.getProfileById(99L);

        assertThat(result).isNotNull();
        verify(realService, times(1)).getProfileById(99L);
    }

    @Test
    @DisplayName("Proxy: Candidate viewing another candidate profile should be denied (HTTP 403 / AccessDeniedException)")
    void shouldDenyUnauthorizedCandidateAccess() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("candidateA", "pass", List.of(new SimpleGrantedAuthority("ROLE_CANDIDATE")))
        );

        // candidateA has profile ID 10
        CandidateProfileResponseDto myProfile = CandidateProfileResponseDto.builder().id(10L).build();
        when(realService.getMyProfile()).thenReturn(myProfile);

        // Trying to view candidateB with profile ID 20
        assertThatThrownBy(() -> proxy.getProfileById(20L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to view profile for this candidate");

        verify(realService, never()).getProfileById(20L);
    }
}