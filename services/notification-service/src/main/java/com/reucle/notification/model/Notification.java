package com.reucle.notification.model;

import java.time.Instant;
import java.util.Objects;

// PM2: notification-service Class & State Diagram -> Notification
// States: PENDING -> SENT | FAILED
public class Notification {
    public enum NotificationStatus {
        PENDING, SENT, FAILED
    }

    public enum Channel {
        EMAIL, SMS, PUSH
    }

    private final String notificationId;
    private final String recipientId;
    private final Channel channel;
    private final String message;
    private NotificationStatus status;
    private final Instant createdAt;

    public Notification(String notificationId, String recipientId, Channel channel, String message) {
        this.notificationId = Objects.requireNonNull(notificationId);
        this.recipientId = Objects.requireNonNull(recipientId);
        this.channel = Objects.requireNonNull(channel);
        this.message = Objects.requireNonNull(message);
        this.status = NotificationStatus.PENDING;
        this.createdAt = Instant.now();
    }

    // State Transition: [PENDING] --(delivered)--> [SENT]
    public void markSent() {
        if (this.status != NotificationStatus.PENDING) {
            throw new IllegalStateException("Cannot mark SENT from state: " + status);
        }
        this.status = NotificationStatus.SENT;
    }

    // State Transition: [PENDING] --(deliveryFailed)--> [FAILED]
    public void markFailed() {
        if (this.status != NotificationStatus.PENDING) {
            throw new IllegalStateException("Cannot mark FAILED from state: " + status);
        }
        this.status = NotificationStatus.FAILED;
    }

    public String getNotificationId() { return notificationId; }
    public String getRecipientId() { return recipientId; }
    public Channel getChannel() { return channel; }
    public String getMessage() { return message; }
    public NotificationStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
