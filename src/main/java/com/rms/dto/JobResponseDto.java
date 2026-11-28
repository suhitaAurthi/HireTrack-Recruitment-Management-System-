package com.rms.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobResponseDto {

    private Long id;
    private String title;
    private String department;
    private String location;
    private String employmentType;
    private String description;
    private String requirements;
    private String category;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private LocalDate applicationDeadline;
    private Integer openPositions;
    private String educationRequirement;
    private Integer minExperienceYears;
    private Set<String> requiredSkills;
    private Boolean active;
    private LocalDateTime createdAt;
}
