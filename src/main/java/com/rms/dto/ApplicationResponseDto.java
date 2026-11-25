package com.rms.dto;

import com.rms.entity.ApplicationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponseDto {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private String jobDepartment;
    private String jobLocation;
    private String jobEmploymentType;

    private Long candidateId;
    private String candidateUsername;
    private String candidateFullName;
    private String candidateEmail;
    private String candidateHeadline;
    private String resumeFileName;

    private ApplicationStatus status;
    private String coverLetter;
    private String recruiterNotes;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
