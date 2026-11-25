package com.rms.dto;

import lombok.*;

import java.time.LocalDateTime;

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
    private Boolean active;
    private LocalDateTime createdAt;
}
