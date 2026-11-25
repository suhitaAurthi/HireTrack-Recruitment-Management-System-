package com.rms.pattern.factory;

import com.rms.entity.Application;
import com.rms.entity.ApplicationStatus;
import com.rms.entity.CandidateProfile;
import com.rms.entity.Job;
import org.springframework.stereotype.Component;

/**
 * Factory Pattern (Pattern #5): ApplicationFactory
 * Encapsulates the instantiation and business default configurations of Application entities.
 */
@Component
public class ApplicationFactory {

    public Application createInitialApplication(CandidateProfile candidateProfile, Job job, String coverLetter) {
        String sanitizedCoverLetter = coverLetter != null ? coverLetter.trim() : null;

        return Application.builder()
                .candidateProfile(candidateProfile)
                .job(job)
                .status(ApplicationStatus.SUBMITTED)
                .coverLetter(sanitizedCoverLetter)
                .recruiterNotes(null)
                .build();
    }
}
