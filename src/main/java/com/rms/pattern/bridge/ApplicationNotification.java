package com.rms.pattern.bridge;

import com.rms.entity.ApplicationStatus;

/**
 * Bridge Pattern (Pattern #6): Abstraction
 * Maintains a reference to the NotificationSender implementor.
 * Decouples the notification domain event from the underlying communication channel.
 */
public abstract class ApplicationNotification {

    protected final NotificationSender sender;

    protected ApplicationNotification(NotificationSender sender) {
        this.sender = sender;
    }

    public abstract void notifyStatusChange(String recipient, String candidateName, String jobTitle, ApplicationStatus status);
}
