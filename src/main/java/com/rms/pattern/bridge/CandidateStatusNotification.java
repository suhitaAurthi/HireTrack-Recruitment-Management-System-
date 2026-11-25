package com.rms.pattern.bridge;

import com.rms.entity.ApplicationStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Bridge Pattern: Refined Abstraction
 * Represents high-level candidate notification business logic.
 */
@Component
public class CandidateStatusNotification extends ApplicationNotification {

    public CandidateStatusNotification(@Qualifier("emailNotificationSender") NotificationSender sender) {
        super(sender);
    }

    @Override
    public void notifyStatusChange(String recipient, String candidateName, String jobTitle, ApplicationStatus status) {
        String subject = "HireTrack Application Update: " + jobTitle + " [" + status + "]";
        String body = String.format("Dear %s, your application for \"%s\" has progressed to status: %s.",
                candidateName, jobTitle, status);

        sender.sendNotification(recipient, subject, body);
    }
}
