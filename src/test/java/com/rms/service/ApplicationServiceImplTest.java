package com.rms.service;

import com.rms.dto.ApplicationApplyRequestDto;
import com.rms.dto.ApplicationResponseDto;
import com.rms.dto.ApplicationStatusUpdateRequestDto;
import com.rms.entity.*;
import com.rms.exception.DuplicateResourceException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.pattern.bridge.CandidateStatusNotification;
import com.rms.pattern.factory.ApplicationFactory;
import com.rms.repository.ApplicationRepository;
import com.rms.repository.CandidateProfileRepository;
import com.rms.repository.JobRepository;
import com.rms.repository.UserRepository;
import com.rms.service.impl.ApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CandidateProfileRepository candidateProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private ApplicationFactory applicationFactory = new ApplicationFactory();

    @Mock
    private CandidateStatusNotification candidateStatusNotification;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private User candidateUser;
    private CandidateProfile candidateProfile;
    private Job job;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("sarah_c", "password", List.of())
        );

        candidateUser = User.builder()
                .id(1L)
                .username("sarah_c")
                .email("sarah@hiretrack.com")
                .firstName("Sarah")
                .lastName("Connor")
                .build();

        candidateProfile = CandidateProfile.builder()
                .id(10L)
                .user(candidateUser)
                .headline("Senior Java Engineer")
                .resumeFileName("sarah_cv.pdf")
                .build();

        job = Job.builder()
                .id(100L)
                .title("Staff Cloud Architect")
                .department("Engineering")
                .location("San Francisco, CA")
                .employmentType("Full-Time")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should successfully apply for a job and dispatch notification via Bridge")
    void shouldSuccessfullyApplyForJob() {
        ApplicationApplyRequestDto request = ApplicationApplyRequestDto.builder()
                .jobId(100L)
                .coverLetter("Excited to lead the cloud migration.")
                .build();

        when(candidateProfileRepository.findByUserUsername("sarah_c")).thenReturn(Optional.of(candidateProfile));
        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByCandidateProfileIdAndJobId(10L, 100L)).thenReturn(false);

        Application persisted = Application.builder()
                .id(500L)
                .candidateProfile(candidateProfile)
                .job(job)
                .status(ApplicationStatus.SUBMITTED)
                .coverLetter("Excited to lead the cloud migration.")
                .appliedAt(LocalDateTime.now())
                .build();

        when(applicationRepository.save(any(Application.class))).thenReturn(persisted);

        ApplicationResponseDto response = applicationService.applyForJob(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(500L);
        assertThat(response.getJobTitle()).isEqualTo("Staff Cloud Architect");
        assertThat(response.getCandidateUsername()).isEqualTo("sarah_c");
        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);

        verify(candidateStatusNotification, times(1))
                .notifyStatusChange(eq("sarah@hiretrack.com"), eq("Sarah"), eq("Staff Cloud Architect"), eq(ApplicationStatus.SUBMITTED));
    }

    @Test
    @DisplayName("Duplicate Prevention: Should throw DuplicateResourceException when applying to same job twice")
    void shouldThrowDuplicateResourceExceptionWhenApplyingTwice() {
        ApplicationApplyRequestDto request = ApplicationApplyRequestDto.builder()
                .jobId(100L)
                .build();

        when(candidateProfileRepository.findByUserUsername("sarah_c")).thenReturn(Optional.of(candidateProfile));
        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByCandidateProfileIdAndJobId(10L, 100L)).thenReturn(true);

        assertThatThrownBy(() -> applicationService.applyForJob(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("You have already submitted an application for the position");

        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when applying without candidate profile")
    void shouldThrowWhenCandidateProfileDoesNotExist() {
        ApplicationApplyRequestDto request = ApplicationApplyRequestDto.builder()
                .jobId(100L)
                .build();

        when(candidateProfileRepository.findByUserUsername("sarah_c")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.applyForJob(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Candidate profile must be created before applying for jobs");
    }

    @Test
    @DisplayName("Should successfully update application status and dispatch notification")
    void shouldSuccessfullyUpdateApplicationStatus() {
        Application existing = Application.builder()
                .id(500L)
                .candidateProfile(candidateProfile)
                .job(job)
                .status(ApplicationStatus.SUBMITTED)
                .build();

        when(applicationRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(existing));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationStatusUpdateRequestDto updateRequest = ApplicationStatusUpdateRequestDto.builder()
                .status(ApplicationStatus.SHORTLISTED)
                .recruiterNotes("Strong technical background.")
                .build();

        ApplicationResponseDto response = applicationService.updateApplicationStatus(500L, updateRequest);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.SHORTLISTED);
        assertThat(response.getRecruiterNotes()).isEqualTo("Strong technical background.");

        verify(candidateStatusNotification, times(1))
                .notifyStatusChange(eq("sarah@hiretrack.com"), eq("Sarah"), eq("Staff Cloud Architect"), eq(ApplicationStatus.SHORTLISTED));
    }

    @Test
    @DisplayName("Should reject updating an application that was withdrawn by candidate")
    void shouldRejectUpdatingWithdrawnApplication() {
        Application withdrawn = Application.builder()
                .id(500L)
                .candidateProfile(candidateProfile)
                .job(job)
                .status(ApplicationStatus.WITHDRAWN)
                .build();

        when(applicationRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(withdrawn));

        ApplicationStatusUpdateRequestDto updateRequest = ApplicationStatusUpdateRequestDto.builder()
                .status(ApplicationStatus.SHORTLISTED)
                .build();

        assertThatThrownBy(() -> applicationService.updateApplicationStatus(500L, updateRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot change status of an application that was withdrawn");
    }

    @Test
    @DisplayName("Candidate can withdraw their own application")
    void shouldAllowCandidateToWithdrawOwnApplication() {
        Application existing = Application.builder()
                .id(500L)
                .candidateProfile(candidateProfile)
                .job(job)
                .status(ApplicationStatus.SUBMITTED)
                .build();

        when(applicationRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(existing));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponseDto response = applicationService.withdrawApplication(500L);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.WITHDRAWN);
    }

    @Test
    @DisplayName("Security: Candidate cannot withdraw another candidate's application")
    void shouldDenyWithdrawingAnotherCandidateApplication() {
        User otherUser = User.builder().username("other_user").build();
        CandidateProfile otherProfile = CandidateProfile.builder().id(99L).user(otherUser).build();
        Application otherApp = Application.builder().id(999L).candidateProfile(otherProfile).job(job).build();

        when(applicationRepository.findByIdWithDetails(999L)).thenReturn(Optional.of(otherApp));

        assertThatThrownBy(() -> applicationService.withdrawApplication(999L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You are not authorized to withdraw another candidate's application");
    }
}
