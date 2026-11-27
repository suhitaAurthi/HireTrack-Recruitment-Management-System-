package com.rms.pattern.adapter;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Adapter Pattern: Concrete Adapter
 * Adapts local filesystem java.nio.file APIs into the common ResumeStorageAdapter contract.
 */
@Component
public class LocalDiskStorageAdapter implements ResumeStorageAdapter {

    private final Path storageDirectory = Paths.get("uploads", "resumes");

    public LocalDiskStorageAdapter() {
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize local resume storage folder", e);
        }
    }

    @Override
    public String storeResume(MultipartFile file, String username) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store an empty file");
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
        }

        if (!fileExtension.equals(".pdf") && !fileExtension.equals(".docx")) {
            throw new IllegalArgumentException("Only PDF and DOCX files are permitted for resumes");
        }

        String sanitizedFileName = username + "_" + UUID.randomUUID() + fileExtension;
        Path destination = storageDirectory.resolve(sanitizedFileName).normalize();

        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return destination.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store resume on local disk", e);
        }
    }

    @Override
    public Resource loadResume(String storedPath) {
        try {
            Path file = Paths.get(storedPath).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Resume file not found or not readable: " + storedPath);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed file path for resume: " + storedPath, e);
        }
    }

    @Override
    public void deleteResume(String storedPath) {
        try {
            Path file = Paths.get(storedPath).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // Log warning or handle cleanup
        }
    }
}