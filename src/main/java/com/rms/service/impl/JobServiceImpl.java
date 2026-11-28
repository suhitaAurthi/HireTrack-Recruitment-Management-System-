package com.rms.service.impl;

import com.rms.dto.JobRequestDto;
import com.rms.dto.JobResponseDto;
import com.rms.entity.Job;
import com.rms.exception.InvalidRequestException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.pattern.flyweight.SkillFlyweightFactory;
import com.rms.repository.ApplicationRepository;
import com.rms.repository.JobRepository;
import com.rms.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final SkillFlyweightFactory skillFlyweightFactory;

    @Override
    public JobResponseDto createJob(JobRequestDto request) {
        rejectPastDeadline(request.getApplicationDeadline());

        Job job = new Job();
        job.setActive(true);
        applyRequest(job, request);

        return mapToDto(jobRepository.save(job));
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponseDto getJobById(Long jobId) {
        Job job = findJob(jobId);

        if (!job.getActive() && !canManageJobs()) {
            throw jobNotFound(jobId);
        }

        return mapToDto(job);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponseDto> getActiveJobs() {
        return jobRepository.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public JobResponseDto updateJob(Long jobId, JobRequestDto request) {
        Job job = findJob(jobId);

        // An unchanged deadline may already be in the past; the rest of the vacancy stays editable.
        if (!Objects.equals(job.getApplicationDeadline(), request.getApplicationDeadline())) {
            rejectPastDeadline(request.getApplicationDeadline());
        }
        applyRequest(job, request);

        return mapToDto(jobRepository.save(job));
    }

    @Override
    public Optional<JobResponseDto> deleteJob(Long jobId) {
        Job job = findJob(jobId);

        if (applicationRepository.existsByJobId(jobId)) {
            job.setActive(false);
            return Optional.of(mapToDto(jobRepository.save(job)));
        }

        jobRepository.delete(job);
        return Optional.empty();
    }

    private Job findJob(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> jobNotFound(jobId));
    }

    private ResourceNotFoundException jobNotFound(Long jobId) {
        return new ResourceNotFoundException("Job vacancy not found with ID: " + jobId);
    }

    private void rejectPastDeadline(LocalDate deadline) {
        if (deadline != null && deadline.isBefore(LocalDate.now())) {
            throw new InvalidRequestException("Application deadline cannot be in the past");
        }
    }

    private boolean canManageJobs() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RECRUITER") || a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void applyRequest(Job job, JobRequestDto request) {
        job.setTitle(request.getTitle().trim());
        job.setDepartment(request.getDepartment().trim());
        job.setLocation(request.getLocation().trim());
        job.setEmploymentType(request.getEmploymentType().trim());
        job.setDescription(request.getDescription().trim());
        job.setRequirements(trimToNull(request.getRequirements()));
        job.setCategory(trimToNull(request.getCategory()));
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setApplicationDeadline(request.getApplicationDeadline());
        job.setOpenPositions(request.getOpenPositions() != null ? request.getOpenPositions() : 1);
        job.setEducationRequirement(trimToNull(request.getEducationRequirement()));
        job.setMinExperienceYears(request.getMinExperienceYears());
        job.setRequiredSkills(normalizeSkills(request.getRequiredSkills()));
    }

    private Set<String> normalizeSkills(Set<String> skills) {
        if (skills == null) {
            return new HashSet<>();
        }
        // The shared flyweight gives vacancies and candidate profiles the same spelling of a skill.
        return skills.stream()
                .map(s -> skillFlyweightFactory.getSkill(s).getCanonicalName())
                .collect(Collectors.toCollection(HashSet::new));
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private JobResponseDto mapToDto(Job job) {
        return JobResponseDto.builder()
                .id(job.getId())
                .title(job.getTitle())
                .department(job.getDepartment())
                .location(job.getLocation())
                .employmentType(job.getEmploymentType())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .category(job.getCategory())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .applicationDeadline(job.getApplicationDeadline())
                .openPositions(job.getOpenPositions())
                .educationRequirement(job.getEducationRequirement())
                .minExperienceYears(job.getMinExperienceYears())
                // Copy while the transaction is open: the entity's set is loaded lazily.
                .requiredSkills(new HashSet<>(job.getRequiredSkills()))
                .active(job.getActive())
                .createdAt(job.getCreatedAt())
                .build();
    }
}
