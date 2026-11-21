package com.rms.service;

import com.rms.dto.CandidateProfileRequestDto;
import com.rms.dto.CandidateProfileResponseDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface CandidateProfileService {

    CandidateProfileResponseDto createOrUpdateProfile(CandidateProfileRequestDto request);

    CandidateProfileResponseDto getMyProfile();

    CandidateProfileResponseDto getProfileById(Long profileId);

    CandidateProfileResponseDto uploadResume(MultipartFile file);

    Resource downloadResume(Long profileId);
}