package com.rms.service;

import com.rms.dto.ApplicationApplyRequestDto;
import com.rms.dto.ApplicationResponseDto;
import com.rms.dto.ApplicationStatusUpdateRequestDto;
import com.rms.dto.JobResponseDto;

import java.util.List;

public interface ApplicationService {

    ApplicationResponseDto applyForJob(ApplicationApplyRequestDto request);

    List<ApplicationResponseDto> getMyApplications();

    List<ApplicationResponseDto> getApplicationsByJob(Long jobId);

    ApplicationResponseDto getApplicationById(Long applicationId);

    ApplicationResponseDto updateApplicationStatus(Long applicationId, ApplicationStatusUpdateRequestDto request);

    ApplicationResponseDto withdrawApplication(Long applicationId);

    List<JobResponseDto> getActiveJobs();
}
