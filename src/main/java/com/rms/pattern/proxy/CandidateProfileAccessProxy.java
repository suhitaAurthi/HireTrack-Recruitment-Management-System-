package com.rms.pattern.proxy;

import com.rms.dto.CandidateProfileRequestDto;
import com.rms.dto.CandidateProfileResponseDto;
import com.rms.service.CandidateProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Primary
public class CandidateProfileAccessProxy implements CandidateProfileService {

    private static final Logger log = LoggerFactory.getLogger(CandidateProfileAccessProxy.class);
    private final CandidateProfileService realService;

    public CandidateProfileAccessProxy(@Qualifier("candidateProfileServiceImpl") CandidateProfileService realService) {
        this.realService = realService;
    }

    @Override
    public CandidateProfileResponseDto createOrUpdateProfile(CandidateProfileRequestDto request) {
        log.info("Audit: Candidate [{}] updating profile", getPrincipalName());
        return realService.createOrUpdateProfile(request);
    }

    @Override
    public CandidateProfileResponseDto getMyProfile() {
        return realService.getMyProfile();
    }

    @Override
    public CandidateProfileResponseDto getProfileById(Long profileId) {
        checkAccessPermission(profileId, "view profile");
        log.info("Audit: User [{}] accessed candidate profile [{}]", getPrincipalName(), profileId);
        return realService.getProfileById(profileId);
    }

    @Override
    public List<CandidateProfileResponseDto> getAllProfiles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required to browse candidate directory");
        }

        boolean isPrivileged = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals("ROLE_ADMIN") || r.equals("ROLE_RECRUITER"));

        if (!isPrivileged) {
            log.warn("Security Alert: Candidate [{}] attempted unauthorized directory browse", auth.getName());
            throw new AccessDeniedException("Candidates are not permitted to browse the talent directory");
        }

        log.info("Audit: Recruiter/Admin [{}] browsed candidate directory", getPrincipalName());
        return realService.getAllProfiles();
    }

    @Override
    public CandidateProfileResponseDto uploadResume(MultipartFile file) {
        log.info("Audit: Candidate [{}] uploading resume: {}", getPrincipalName(), file.getOriginalFilename());
        return realService.uploadResume(file);
    }

    @Override
    public Resource downloadResume(Long profileId) {
        checkAccessPermission(profileId, "download resume");
        log.info("Audit: User [{}] downloaded resume for profile [{}]", getPrincipalName(), profileId);
        return realService.downloadResume(profileId);
    }

    private void checkAccessPermission(Long profileId, String action) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required to " + action);
        }

        boolean isPrivileged = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals("ROLE_ADMIN") || r.equals("ROLE_RECRUITER"));

        if (!isPrivileged) {
            CandidateProfileResponseDto myProfile = realService.getMyProfile();
            if (myProfile == null || !myProfile.getId().equals(profileId)) {
                log.warn("Security Alert: User [{}] attempted unauthorized {} on profile [{}]", auth.getName(), action, profileId);
                throw new AccessDeniedException("You do not have permission to " + action + " for this candidate");
            }
        }
    }

    private String getPrincipalName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "ANONYMOUS";
    }
}