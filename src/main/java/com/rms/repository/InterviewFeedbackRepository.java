package com.rms.repository;

import com.rms.entity.InterviewFeedback;
import com.rms.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewFeedbackRepository extends JpaRepository<InterviewFeedback, Long> {

    /** Feedback for one interview (at most one, due to the One-to-One mapping). */
    Optional<InterviewFeedback> findByInterviewId(Long interviewId);

    /** Prevents submitting a second feedback for the same interview. */
    boolean existsByInterviewId(Long interviewId);

    /** All feedback for every interview round of one application. */
    List<InterviewFeedback> findByInterviewApplicationId(Long applicationId);

    List<InterviewFeedback> findByRecommendation(Recommendation recommendation);
}
