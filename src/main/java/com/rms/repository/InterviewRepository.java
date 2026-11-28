package com.rms.repository;

import com.rms.entity.Interview;
import com.rms.entity.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    /** All interviews (rounds) for one application, earliest first. */
    List<Interview> findByApplicationIdOrderByScheduledAtAsc(Long applicationId);

    /** All interviews assigned to an interviewer, earliest first. */
    List<Interview> findByInterviewerIdOrderByScheduledAtAsc(Long interviewerId);

    /** Interviews scheduled within a date/time range (inclusive). */
    List<Interview> findByScheduledAtBetweenOrderByScheduledAtAsc(LocalDateTime start, LocalDateTime end);

    /** An interviewer's interviews within a range - used to detect double-booking. */
    List<Interview> findByInterviewerIdAndScheduledAtBetween(Long interviewerId, LocalDateTime start, LocalDateTime end);

    List<Interview> findByStatus(InterviewStatus status);

    /** Count by status - used by the analytics dashboard. */
    long countByStatus(InterviewStatus status);
}
