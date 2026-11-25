package com.rms.pattern.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Bridge Pattern: Concrete Implementor B
 * Delivers application status notifications via In-App Dashboard alerts.
 */
@Component("inAppNotificationSender")
public class InAppNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(InAppNotificationSender.class);

    @Override
    public void sendNotification(String recipient, String subject, String body) {
        log.info("[BRIDGE: IN-APP ALERT] Pushed for [{}] | Alert: \"{}\"", recipient, body);
    }

    @Override
    public String getChannelType() {
        return "IN_APP";
    }
}
