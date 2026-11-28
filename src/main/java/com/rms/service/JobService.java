package com.rms.service;

import com.rms.dto.JobRequestDto;
import com.rms.dto.JobResponseDto;

import java.util.List;
import java.util.Optional;

public interface JobService {

    JobResponseDto createJob(JobRequestDto request);

    /** Closed vacancies are only visible to recruiters and administrators. */
    JobResponseDto getJobById(Long jobId);

    List<JobResponseDto> getActiveJobs();

    JobResponseDto updateJob(Long jobId, JobRequestDto request);

    /**
     * Deletes a vacancy. A vacancy that already has applications is closed instead,
     * so that candidates keep their application history.
     *
     * @return the closed vacancy, or empty when it was permanently deleted
     */
    Optional<JobResponseDto> deleteJob(Long jobId);
}
