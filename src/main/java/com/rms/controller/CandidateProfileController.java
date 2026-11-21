package com.rms.controller;

import com.rms.dto.CandidateProfileRequestDto;
import com.rms.dto.CandidateProfileResponseDto;
import com.rms.service.CandidateProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/candidate/profile")
@RequiredArgsConstructor
public class CandidateProfileController {

    private final CandidateProfileService candidateProfileService;

    @PostMapping
    public ResponseEntity<CandidateProfileResponseDto> createOrUpdateProfile(@Valid @RequestBody CandidateProfileRequestDto request) {
        CandidateProfileResponseDto response = candidateProfileService.createOrUpdateProfile(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<CandidateProfileResponseDto> getMyProfile() {
        return ResponseEntity.ok(candidateProfileService.getMyProfile());
    }

    @GetMapping
    public ResponseEntity<List<CandidateProfileResponseDto>> getAllProfiles() {
        return ResponseEntity.ok(candidateProfileService.getAllProfiles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateProfileResponseDto> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(candidateProfileService.getProfileById(id));
    }

    @PostMapping("/resume")
    public ResponseEntity<CandidateProfileResponseDto> uploadResume(@RequestParam("file") MultipartFile file) {
        CandidateProfileResponseDto response = candidateProfileService.uploadResume(file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/resume")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long id) {
        Resource resumeResource = candidateProfileService.downloadResume(id);
        String filename = resumeResource.getFilename() != null ? resumeResource.getFilename() : "resume.pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resumeResource);
    }

    @GetMapping("/resume")
    public ResponseEntity<Resource> downloadMyResume() {
        CandidateProfileResponseDto myProfile = candidateProfileService.getMyProfile();
        return downloadResume(myProfile.getId());
    }
}