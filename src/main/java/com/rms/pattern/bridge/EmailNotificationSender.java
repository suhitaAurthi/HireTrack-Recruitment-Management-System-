package com.rms.pattern.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Bridge Pattern: Concrete Implementor A
 * Delivers application status notifications via Email channel.
 */
@Component("emailNotificationSender")
public class EmailNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    @Override
    public void sendNotification(String recipient, String subject, String body) {
        log.info("[BRIDGE: EMAIL] Dispatched to <{}> | Subject: \"{}\" | Body: {}", recipient, subject, body);
    }

    @Override
    public String getChannelType() {
        return "EMAIL";
    }
}
