package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * In-app notification shown to a user (candidate, recruiter or admin).
 *
 * Relationship: Many notifications belong to one User (recipient).
 * Persisted counterpart to the Bridge pattern's InAppNotificationSender.
 */
@Entity
@Table(
    name = "notifications",
    indexes = @Index(name = "idx_notification_recipient_read", columnList = "recipient_id, is_read")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false, length = 500)
    private String message;

    /** "read" is a reserved word in some databases, so the column is named is_read. */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean read = false;

    /** Optional category; defaults to GENERAL. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    @Builder.Default
    private NotificationType type = NotificationType.GENERAL;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
