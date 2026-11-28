package com.rms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Evaluation recorded by the interviewer after an interview.
 *
 * Relationship: One-to-One with Interview (each interview has at most one feedback record).
 * Scores are on a 0-10 scale.
 */
@Entity
@Table(name = "interview_feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class InterviewFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false, unique = true)
    private Interview interview;

    @DecimalMin("0.0")
    @DecimalMax("10.0")
    @Column(name = "technical_score", nullable = false)
    private Double technicalScore;

    @DecimalMin("0.0")
    @DecimalMax("10.0")
    @Column(name = "communication_score", nullable = false)
    private Double communicationScore;

    @DecimalMin("0.0")
    @DecimalMax("10.0")
    @Column(name = "overall_score", nullable = false)
    private Double overallScore;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Recommendation recommendation;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
