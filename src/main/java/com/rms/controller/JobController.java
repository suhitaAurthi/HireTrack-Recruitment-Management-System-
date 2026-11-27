package com.rms.controller;

import com.rms.dto.JobResponseDto;
import com.rms.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final ApplicationService applicationService;

    @GetMapping("/active")
    public ResponseEntity<List<JobResponseDto>> getActiveJobs() {
        return ResponseEntity.ok(applicationService.getActiveJobs());
    }
}
