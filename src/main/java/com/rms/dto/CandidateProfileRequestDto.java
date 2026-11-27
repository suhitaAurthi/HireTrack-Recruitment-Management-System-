package com.rms.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfileRequestDto {

    @Size(max = 150, message = "Headline cannot exceed 150 characters")
    private String headline;

    private String summary;

    @Size(max = 30, message = "Phone number cannot exceed 30 characters")
    private String phone;

    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private String location;

    private String education;

    private String experience;

    @Builder.Default
    private Set<String> skills = new HashSet<>();
}