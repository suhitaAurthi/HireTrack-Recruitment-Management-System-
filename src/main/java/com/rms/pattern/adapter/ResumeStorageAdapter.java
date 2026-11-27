package com.rms.pattern.adapter;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Adapter Pattern: Target Interface
 * Decouples CandidateService from concrete storage mechanisms (Local Disk vs Cloud S3).
 */
public interface ResumeStorageAdapter {

    String storeResume(MultipartFile file, String username);

    Resource loadResume(String storedPath);

    void deleteResume(String storedPath);
}