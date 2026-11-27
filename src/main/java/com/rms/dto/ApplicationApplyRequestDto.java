package com.rms.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationApplyRequestDto {

    @NotNull(message = "Job ID is required to submit an application")
    private Long jobId;

    @Size(max = 2000, message = "Cover letter must not exceed 2000 characters")
    private String coverLetter;
}
