package com.rms.service.impl;

import com.rms.dto.RegisterRequestDto;
import com.rms.dto.UserResponseDto;
import com.rms.entity.Role;
import com.rms.entity.RoleType;
import com.rms.entity.User;
import com.rms.exception.DuplicateResourceException;
import com.rms.repository.RoleRepository;
import com.rms.repository.UserRepository;
import com.rms.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDto register(RegisterRequestDto request) {
        // 1. Verify username is unique
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
        }

        // 2. Verify email is unique
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered");
        }

        // 3. Determine and fetch role (default to ROLE_CANDIDATE if not specified)
        RoleType targetRoleType = (request.getRole() != null) ? request.getRole() : RoleType.ROLE_CANDIDATE;
        Role role = roleRepository.findByName(targetRoleType)
                .orElseGet(() -> roleRepository.save(Role.builder().name(targetRoleType).build()));

        // 4. Encrypt password using BCrypt
        String encryptedPassword = passwordEncoder.encode(request.getPassword());

        // 5. Build and save user
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encryptedPassword)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        // 6. Map to safe response DTO
        return mapToResponseDto(savedUser);
    }

    private UserResponseDto mapToResponseDto(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.isEnabled())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
