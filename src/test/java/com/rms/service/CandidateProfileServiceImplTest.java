package com.rms.service;

import com.rms.dto.CandidateProfileRequestDto;
import com.rms.dto.CandidateProfileResponseDto;
import com.rms.entity.CandidateProfile;
import com.rms.entity.Role;
import com.rms.entity.RoleType;
import com.rms.entity.User;
import com.rms.pattern.adapter.ResumeStorageAdapter;
import com.rms.pattern.flyweight.Skill;
import com.rms.pattern.flyweight.SkillFlyweightFactory;
import com.rms.repository.CandidateProfileRepository;
import com.rms.repository.UserRepository;
import com.rms.service.impl.CandidateProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateProfileServiceImplTest {

    @Mock
    private CandidateProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SkillFlyweightFactory skillFlyweightFactory;

    @Mock
    private ResumeStorageAdapter resumeStorageAdapter;

    @InjectMocks
    private CandidateProfileServiceImpl candidateProfileService;

    private User candidateUser;
    private CandidateProfile profile;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().id(1L).name(RoleType.ROLE_CANDIDATE).build();
        candidateUser = User.builder()
                .id(1L)
                .username("candidate1")
                .email("candidate1@rms.com")
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of(role))
                .build();

        profile = CandidateProfile.builder()
                .id(10L)
                .user(candidateUser)
                .headline("Software Engineer")
                .build();

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("candidate1");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should create or update profile with flyweight skills")
    void shouldCreateOrUpdateProfile() {
        CandidateProfileRequestDto request = CandidateProfileRequestDto.builder()
                .headline("Full Stack Dev")
                .skills(Set.of("Java", "Spring Boot"))
                .build();

        when(userRepository.findByUsername("candidate1")).thenReturn(Optional.of(candidateUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(skillFlyweightFactory.getSkill("Java")).thenReturn(new Skill("Java", "Programming Language"));
        when(skillFlyweightFactory.getSkill("Spring Boot")).thenReturn(new Skill("Spring Boot", "Framework"));
        when(profileRepository.save(any(CandidateProfile.class))).thenReturn(profile);

        CandidateProfileResponseDto result = candidateProfileService.createOrUpdateProfile(request);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("candidate1");
        verify(profileRepository, times(1)).save(any(CandidateProfile.class));
    }

    @Test
    @DisplayName("Should upload resume using storage adapter")
    void shouldUploadResumeUsingAdapter() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "dummy content".getBytes());

        when(userRepository.findByUsername("candidate1")).thenReturn(Optional.of(candidateUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(resumeStorageAdapter.storeResume(file, "candidate1")).thenReturn("uploads/resumes/candidate1_resume.pdf");
        when(profileRepository.save(any(CandidateProfile.class))).thenReturn(profile);

        CandidateProfileResponseDto result = candidateProfileService.uploadResume(file);

        assertThat(result).isNotNull();
        verify(resumeStorageAdapter, times(1)).storeResume(file, "candidate1");
        verify(profileRepository, times(1)).save(any(CandidateProfile.class));
    }
}