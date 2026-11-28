package com.reucle.notification.sender;

import com.reucle.notification.model.Notification;

// PM2: notification-service Collaboration/Sequence Diagram -> INotificationSender
public interface INotificationSender {
    boolean send(Notification notification);
}
