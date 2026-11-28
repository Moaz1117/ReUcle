package com.reucle.notification.repository;

import com.reucle.notification.model.Notification;
import java.util.Optional;

// PM2: notification-service Class/Collaboration Diagram -> INotificationRepository
public interface INotificationRepository {
    Optional<Notification> findById(String notificationId);
    Notification save(Notification notification);
}
