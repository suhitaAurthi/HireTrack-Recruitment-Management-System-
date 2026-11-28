package com.rms.controller;

import com.rms.dto.JobRequestDto;
import com.rms.dto.JobResponseDto;
import com.rms.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping("/active")
    public ResponseEntity<List<JobResponseDto>> getActiveJobs() {
        return ResponseEntity.ok(jobService.getActiveJobs());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JobResponseDto> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<JobResponseDto> createJob(@Valid @RequestBody JobRequestDto request) {
        JobResponseDto created = jobService.createJob(request);
        return ResponseEntity.created(URI.create("/api/jobs/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<JobResponseDto> updateJob(@PathVariable Long id,
                                                    @Valid @RequestBody JobRequestDto request) {
        return ResponseEntity.ok(jobService.updateJob(id, request));
    }

    /** 204 when the vacancy was deleted; 200 with the vacancy when it was closed instead. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<JobResponseDto> deleteJob(@PathVariable Long id) {
        return jobService.deleteJob(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
