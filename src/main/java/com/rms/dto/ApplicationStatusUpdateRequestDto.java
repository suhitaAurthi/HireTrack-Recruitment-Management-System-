package com.rms.dto;

import com.rms.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStatusUpdateRequestDto {

    @NotNull(message = "Application status is required")
    private ApplicationStatus status;

    @Size(max = 1000, message = "Recruiter notes must not exceed 1000 characters")
    private String recruiterNotes;
}
