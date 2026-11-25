package com.rms.service.impl;

import com.rms.dto.ApplicationApplyRequestDto;
import com.rms.dto.ApplicationResponseDto;
import com.rms.dto.ApplicationStatusUpdateRequestDto;
import com.rms.dto.JobResponseDto;
import com.rms.entity.*;
import com.rms.exception.DuplicateResourceException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.pattern.bridge.CandidateStatusNotification;
import com.rms.pattern.factory.ApplicationFactory;
import com.rms.repository.ApplicationRepository;
import com.rms.repository.CandidateProfileRepository;
import com.rms.repository.JobRepository;
import com.rms.repository.UserRepository;
import com.rms.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final UserRepository userRepository;
    private final ApplicationFactory applicationFactory;
    private final CandidateStatusNotification candidateStatusNotification;

    @Override
    public ApplicationResponseDto applyForJob(ApplicationApplyRequestDto request) {
        String username = getAuthenticatedUsername();

        CandidateProfile candidateProfile = candidateProfileRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile must be created before applying for jobs"));

        Job job = jobRepository.findById(request.getJobId())
                .filter(Job::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Job vacancy not found or no longer active with ID: " + request.getJobId()));

        if (applicationRepository.existsByCandidateProfileIdAndJobId(candidateProfile.getId(), job.getId())) {
            throw new DuplicateResourceException("You have already submitted an application for the position: " + job.getTitle());
        }

        Application application = applicationFactory.createInitialApplication(candidateProfile, job, request.getCoverLetter());
        Application saved = applicationRepository.save(application);

        User user = candidateProfile.getUser();
        String candidateName = (user.getFirstName() != null) ? user.getFirstName() : user.getUsername();
        candidateStatusNotification.notifyStatusChange(user.getEmail(), candidateName, job.getTitle(), ApplicationStatus.SUBMITTED);

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getMyApplications() {
        String username = getAuthenticatedUsername();

        CandidateProfile candidateProfile = candidateProfileRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + username));

        return applicationRepository.findByCandidateProfileIdWithJob(candidateProfile.getId())
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getApplicationsByJob(Long jobId) {
        if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job vacancy not found with ID: " + jobId);
        }

        return applicationRepository.findByJobIdWithCandidate(jobId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponseDto getApplicationById(Long applicationId) {
        Application application = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        return mapToDto(application);
    }

    @Override
    public ApplicationResponseDto updateApplicationStatus(Long applicationId, ApplicationStatusUpdateRequestDto request) {
        Application application = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new IllegalStateException("Cannot change status of an application that was withdrawn by the candidate");
        }

        application.setStatus(request.getStatus());
        if (request.getRecruiterNotes() != null) {
            application.setRecruiterNotes(request.getRecruiterNotes().trim());
        }

        Application updated = applicationRepository.save(application);

        User user = updated.getCandidateProfile().getUser();
        String candidateName = (user.getFirstName() != null) ? user.getFirstName() : user.getUsername();
        candidateStatusNotification.notifyStatusChange(user.getEmail(), candidateName, updated.getJob().getTitle(), request.getStatus());

        return mapToDto(updated);
    }

    @Override
    public ApplicationResponseDto withdrawApplication(Long applicationId) {
        String username = getAuthenticatedUsername();

        Application application = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        if (!application.getCandidateProfile().getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to withdraw another candidate's application");
        }

        if (application.getStatus() == ApplicationStatus.HIRED) {
            throw new IllegalStateException("Cannot withdraw an application that has already been accepted/hired");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        Application updated = applicationRepository.save(application);

        return mapToDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponseDto> getActiveJobs() {
        return jobRepository.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(j -> JobResponseDto.builder()
                        .id(j.getId())
                        .title(j.getTitle())
                        .department(j.getDepartment())
                        .location(j.getLocation())
                        .employmentType(j.getEmploymentType())
                        .description(j.getDescription())
                        .requirements(j.getRequirements())
                        .active(j.getActive())
                        .createdAt(j.getCreatedAt())
                        .build())
                .toList();
    }

    private ApplicationResponseDto mapToDto(Application a) {
        CandidateProfile profile = a.getCandidateProfile();
        User user = profile != null ? profile.getUser() : null;
        Job job = a.getJob();

        String fullName = (user != null && user.getFirstName() != null)
                ? user.getFirstName() + (user.getLastName() != null ? " " + user.getLastName() : "")
                : (user != null ? user.getUsername() : "Unknown");

        return ApplicationResponseDto.builder()
                .id(a.getId())
                .jobId(job != null ? job.getId() : null)
                .jobTitle(job != null ? job.getTitle() : null)
                .jobDepartment(job != null ? job.getDepartment() : null)
                .jobLocation(job != null ? job.getLocation() : null)
                .jobEmploymentType(job != null ? job.getEmploymentType() : null)
                .candidateId(profile != null ? profile.getId() : null)
                .candidateUsername(user != null ? user.getUsername() : null)
                .candidateFullName(fullName)
                .candidateEmail(user != null ? user.getEmail() : null)
                .candidateHeadline(profile != null ? profile.getHeadline() : null)
                .resumeFileName(profile != null ? profile.getResumeFileName() : null)
                .status(a.getStatus())
                .coverLetter(a.getCoverLetter())
                .recruiterNotes(a.getRecruiterNotes())
                .appliedAt(a.getAppliedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private String getAuthenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User must be authenticated to perform this operation");
        }
        return auth.getName();
    }
}
