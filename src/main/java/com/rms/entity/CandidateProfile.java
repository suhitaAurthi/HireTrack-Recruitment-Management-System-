package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "candidate_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-One mapping: Each registered user with ROLE_CANDIDATE can have one candidate profile.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(length = 150)
    private String headline;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(length = 30)
    private String phone;

    @Column(length = 100)
    private String location;

    @Column(name = "resume_url")
    private String resumeUrl;

    @Column(name = "resume_file_name")
    private String resumeFileName;

    @Column(columnDefinition = "TEXT")
    private String education;

    @Column(columnDefinition = "TEXT")
    private String experience;

    /**
     * Collection table for multi-valued skills list (e.g., Java, Spring Boot, PostgreSQL, Docker).
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "candidate_skills",
        joinColumns = @JoinColumn(name = "candidate_profile_id")
    )
    @Column(name = "skill", nullable = false, length = 50)
    @Builder.Default
    private Set<String> skills = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}