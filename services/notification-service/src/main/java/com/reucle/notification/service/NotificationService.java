package com.reucle.notification.service;

import com.reucle.notification.model.Notification;
import com.reucle.notification.repository.INotificationRepository;
import com.reucle.notification.sender.INotificationSender;
import java.util.UUID;

// PM2: notification-service Class/Collaboration/Sequence Diagram -> NotificationService
public class NotificationService {
    private final INotificationRepository repository;
    private final INotificationSender sender;

    public NotificationService(INotificationRepository repository, INotificationSender sender) {
        this.repository = repository;
        this.sender = sender;
    }

    // PM2: notification-service Sequence (Dispatch Alert) -> NotificationService.dispatchNotification
    public Notification dispatchNotification(String recipientId, Notification.Channel channel, String message) {
        Notification notification = new Notification(UUID.randomUUID().toString(), recipientId, channel, message);
        repository.save(notification);

        boolean delivered = sender.send(notification);
        if (delivered) {
            notification.markSent();
        } else {
            notification.markFailed();
        }
        return repository.save(notification);
    }
}
