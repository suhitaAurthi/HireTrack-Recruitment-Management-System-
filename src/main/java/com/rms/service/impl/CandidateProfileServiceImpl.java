package com.rms.service.impl;

import com.rms.dto.CandidateProfileRequestDto;
import com.rms.dto.CandidateProfileResponseDto;
import com.rms.entity.CandidateProfile;
import com.rms.entity.User;
import com.rms.exception.ResourceNotFoundException;
import com.rms.pattern.adapter.ResumeStorageAdapter;
import com.rms.pattern.flyweight.Skill;
import com.rms.pattern.flyweight.SkillFlyweightFactory;
import com.rms.repository.CandidateProfileRepository;
import com.rms.repository.UserRepository;
import com.rms.service.CandidateProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core Candidate Profile Service (Real Subject)
 * Implements business operations using Flyweight for skills and Adapter for resume storage.
 */
@Service("candidateProfileServiceImpl")
@RequiredArgsConstructor
public class CandidateProfileServiceImpl implements CandidateProfileService {

    private final CandidateProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final SkillFlyweightFactory skillFlyweightFactory;
    private final ResumeStorageAdapter resumeStorageAdapter;

    @Override
    @Transactional
    public CandidateProfileResponseDto createOrUpdateProfile(CandidateProfileRequestDto request) {
        User currentUser = getCurrentAuthenticatedUser();

        CandidateProfile profile = profileRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> CandidateProfile.builder()
                        .user(currentUser)
                        .skills(new HashSet<>())
                        .build());

        profile.setHeadline(request.getHeadline());
        profile.setSummary(request.getSummary());
        profile.setPhone(request.getPhone());
        profile.setLocation(request.getLocation());
        profile.setEducation(request.getEducation());
        profile.setExperience(request.getExperience());

        // Apply Flyweight pattern: normalize and pool skill strings
        if (request.getSkills() != null) {
            Set<String> normalizedSkills = request.getSkills().stream()
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .map(s -> skillFlyweightFactory.getSkill(s).getCanonicalName())
                    .collect(Collectors.toSet());
            profile.setSkills(normalizedSkills);
        }

        CandidateProfile saved = profileRepository.save(profile);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateProfileResponseDto getMyProfile() {
        User currentUser = getCurrentAuthenticatedUser();
        CandidateProfile profile = profileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + currentUser.getUsername()));
        return mapToDto(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateProfileResponseDto getProfileById(Long profileId) {
        CandidateProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found with ID: " + profileId));
        return mapToDto(profile);
    }

    @Override
    @Transactional
    public CandidateProfileResponseDto uploadResume(MultipartFile file) {
        User currentUser = getCurrentAuthenticatedUser();

        CandidateProfile profile = profileRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> CandidateProfile.builder()
                        .user(currentUser)
                        .skills(new HashSet<>())
                        .build());

        // Delete old resume if present via Adapter
        if (profile.getResumeUrl() != null) {
            resumeStorageAdapter.deleteResume(profile.getResumeUrl());
        }

        // Store new resume via Adapter
        String storedPath = resumeStorageAdapter.storeResume(file, currentUser.getUsername());
        profile.setResumeUrl(storedPath);
        profile.setResumeFileName(file.getOriginalFilename());

        CandidateProfile saved = profileRepository.save(profile);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadResume(Long profileId) {
        CandidateProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with ID: " + profileId));

        if (profile.getResumeUrl() == null) {
            throw new ResourceNotFoundException("No resume uploaded for candidate profile ID: " + profileId);
        }

        return resumeStorageAdapter.loadResume(profile.getResumeUrl());
    }

    private User getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated candidate user found in security context");
        }

        return userRepository.findByUsername(auth.getName())
                .or(() -> userRepository.findByEmail(auth.getName()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    private CandidateProfileResponseDto mapToDto(CandidateProfile profile) {
        User user = profile.getUser();
        return CandidateProfileResponseDto.builder()
                .id(profile.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .headline(profile.getHeadline())
                .summary(profile.getSummary())
                .phone(profile.getPhone())
                .location(profile.getLocation())
                .resumeUrl(profile.getResumeUrl())
                .resumeFileName(profile.getResumeFileName())
                .education(profile.getEducation())
                .experience(profile.getExperience())
                .skills(profile.getSkills())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}