package com.rms.service;

import com.rms.dto.RegisterRequestDto;
import com.rms.dto.UserResponseDto;

public interface AuthService {

    /**
     * Registers a new user with BCrypt encrypted password and assigned role.
     * 
     * @param request The registration details
     * @return UserResponseDto containing safe user information
     */
    UserResponseDto register(RegisterRequestDto request);
}
