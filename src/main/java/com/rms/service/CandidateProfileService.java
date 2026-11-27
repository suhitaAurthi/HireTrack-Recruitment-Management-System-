package com.rms.service;

import com.rms.dto.CandidateProfileRequestDto;
import com.rms.dto.CandidateProfileResponseDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CandidateProfileService {

    CandidateProfileResponseDto createOrUpdateProfile(CandidateProfileRequestDto request);

    CandidateProfileResponseDto getMyProfile();

    CandidateProfileResponseDto getProfileById(Long profileId);

    List<CandidateProfileResponseDto> getAllProfiles();

    CandidateProfileResponseDto uploadResume(MultipartFile file);

    Resource downloadResume(Long profileId);
}