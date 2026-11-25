package com.rms.pattern.bridge;

/**
 * Bridge Pattern (Pattern #6): Implementor Interface
 * Defines the low-level delivery channel for recruitment notifications.
 */
public interface NotificationSender {

    void sendNotification(String recipient, String subject, String body);

    String getChannelType();
}
