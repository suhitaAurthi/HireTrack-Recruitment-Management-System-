package com.rms.service;

import com.rms.dto.LoginRequestDto;
import com.rms.dto.RegisterRequestDto;
import com.rms.dto.UserResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    UserResponseDto register(RegisterRequestDto request);

    UserResponseDto login(LoginRequestDto request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    UserResponseDto getCurrentUser();
}