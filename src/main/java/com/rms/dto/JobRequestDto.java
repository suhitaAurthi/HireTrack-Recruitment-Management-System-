package com.rms.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/** Fields a recruiter sends when creating or editing a vacancy. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRequestDto {

    @NotBlank(message = "Job title is required")
    @Size(max = 120, message = "Job title must not exceed 120 characters")
    private String title;

    @NotBlank(message = "Department is required")
    @Size(max = 80, message = "Department must not exceed 80 characters")
    private String department;

    @NotBlank(message = "Location is required")
    @Size(max = 80, message = "Location must not exceed 80 characters")
    private String location;

    @NotBlank(message = "Employment type is required")
    @Size(max = 50, message = "Employment type must not exceed 50 characters")
    private String employmentType;

    @NotBlank(message = "Job description is required")
    private String description;

    private String requirements;

    @Size(max = 80, message = "Category must not exceed 80 characters")
    private String category;

    @PositiveOrZero(message = "Minimum salary cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Minimum salary allows up to 10 digits and 2 decimals")
    private BigDecimal salaryMin;

    @PositiveOrZero(message = "Maximum salary cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Maximum salary allows up to 10 digits and 2 decimals")
    private BigDecimal salaryMax;

    private LocalDate applicationDeadline;

    /** Defaults to 1 when omitted. */
    @Min(value = 1, message = "At least one position is required")
    private Integer openPositions;

    @Size(max = 200, message = "Education requirement must not exceed 200 characters")
    private String educationRequirement;

    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 60, message = "Experience must not exceed 60 years")
    private Integer minExperienceYears;

    private Set<
            @NotBlank(message = "Skill cannot be blank")
            @Size(max = 50, message = "Skill must not exceed 50 characters") String> requiredSkills;

    @JsonIgnore
    @AssertTrue(message = "Maximum salary cannot be lower than minimum salary")
    public boolean isSalaryRangeValid() {
        return salaryMin == null || salaryMax == null || salaryMax.compareTo(salaryMin) >= 0;
    }
}
