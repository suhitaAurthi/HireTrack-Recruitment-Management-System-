package com.rms.service;

import com.rms.dto.JobRequestDto;
import com.rms.dto.JobResponseDto;
import com.rms.entity.Job;
import com.rms.exception.InvalidRequestException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.pattern.flyweight.SkillFlyweightFactory;
import com.rms.repository.ApplicationRepository;
import com.rms.repository.JobRepository;
import com.rms.service.impl.JobServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    private JobService jobService;

    @BeforeEach
    void setUp() {
        jobService = new JobServiceImpl(jobRepository, applicationRepository, new SkillFlyweightFactory());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should create an active vacancy with trimmed text and one position by default")
    void shouldCreateJobWithDefaults() {
        returnSavedJob();
        JobRequestDto request = validRequest()
                .title("  Backend Engineer  ")
                .category("   ")
                .requiredSkills(Set.of(" Java "))
                .build();

        JobResponseDto created = jobService.createJob(request);

        assertThat(created.getTitle()).isEqualTo("Backend Engineer");
        assertThat(created.getCategory()).isNull();
        assertThat(created.getOpenPositions()).isEqualTo(1);
        assertThat(created.getRequiredSkills()).containsExactly("Java");
        assertThat(created.getActive()).isTrue();
    }

    @Test
    @DisplayName("Should reject a new vacancy whose deadline is in the past")
    void shouldRejectPastDeadlineOnCreate() {
        JobRequestDto request = validRequest().applicationDeadline(LocalDate.now().minusDays(1)).build();

        assertThatThrownBy(() -> jobService.createJob(request))
                .isInstanceOf(InvalidRequestException.class);
        verify(jobRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should replace the details of an existing vacancy")
    void shouldUpdateJob() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(existingJob(true)));
        returnSavedJob();
        JobRequestDto request = validRequest().title("Lead Engineer").openPositions(4).build();

        JobResponseDto updated = jobService.updateJob(1L, request);

        assertThat(updated.getTitle()).isEqualTo("Lead Engineer");
        assertThat(updated.getOpenPositions()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should allow editing a vacancy whose unchanged deadline has passed")
    void shouldKeepUnchangedPastDeadline() {
        LocalDate passed = LocalDate.now().minusDays(5);
        Job job = existingJob(true);
        job.setApplicationDeadline(passed);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        returnSavedJob();

        JobResponseDto updated = jobService.updateJob(1L, validRequest().applicationDeadline(passed).build());

        assertThat(updated.getApplicationDeadline()).isEqualTo(passed);
    }

    @Test
    @DisplayName("Should reject moving the deadline to a past date")
    void shouldRejectNewPastDeadlineOnUpdate() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(existingJob(true)));
        JobRequestDto request = validRequest().applicationDeadline(LocalDate.now().minusDays(1)).build();

        assertThatThrownBy(() -> jobService.updateJob(1L, request))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("Should fail to update a vacancy that does not exist")
    void shouldThrowWhenUpdatingMissingJob() {
        when(jobRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.updateJob(9L, validRequest().build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should permanently delete a vacancy without applications")
    void shouldDeleteJobWithoutApplications() {
        Job job = existingJob(true);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobId(1L)).thenReturn(false);

        assertThat(jobService.deleteJob(1L)).isEmpty();
        verify(jobRepository).delete(job);
    }

    @Test
    @DisplayName("Should close instead of delete a vacancy that has applications")
    void shouldCloseJobWithApplications() {
        Job job = existingJob(true);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobId(1L)).thenReturn(true);
        returnSavedJob();

        Optional<JobResponseDto> closed = jobService.deleteJob(1L);

        assertThat(closed).get().extracting(JobResponseDto::getActive).isEqualTo(false);
        verify(jobRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should hide a closed vacancy from candidates")
    void shouldHideClosedJobFromCandidate() {
        signInAs("ROLE_CANDIDATE");
        when(jobRepository.findById(1L)).thenReturn(Optional.of(existingJob(false)));

        assertThatThrownBy(() -> jobService.getJobById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should show a closed vacancy to recruiters")
    void shouldShowClosedJobToRecruiter() {
        signInAs("ROLE_RECRUITER");
        when(jobRepository.findById(1L)).thenReturn(Optional.of(existingJob(false)));

        assertThat(jobService.getJobById(1L).getActive()).isFalse();
    }

    @Test
    @DisplayName("Should list the active vacancies")
    void shouldListActiveJobs() {
        when(jobRepository.findByActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(existingJob(true)));

        assertThat(jobService.getActiveJobs())
                .extracting(JobResponseDto::getTitle)
                .containsExactly("Backend Engineer");
    }

    private void returnSavedJob() {
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void signInAs(String role) {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("user", "password", role));
    }

    private Job existingJob(boolean active) {
        return Job.builder()
                .id(1L)
                .title("Backend Engineer")
                .department("Engineering")
                .location("Khulna")
                .employmentType("Full-Time")
                .description("Build REST APIs.")
                .active(active)
                .build();
    }

    private JobRequestDto.JobRequestDtoBuilder validRequest() {
        return JobRequestDto.builder()
                .title("Backend Engineer")
                .department("Engineering")
                .location("Khulna")
                .employmentType("Full-Time")
                .description("Build REST APIs.");
    }
}
