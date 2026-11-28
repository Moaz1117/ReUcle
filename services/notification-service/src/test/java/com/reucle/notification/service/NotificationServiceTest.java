package com.reucle.notification.service;

import com.reucle.notification.model.Notification;
import com.reucle.notification.repository.INotificationRepository;
import com.reucle.notification.sender.INotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// PM2 Test Map: notification-service Class, Collaboration, Sequence & State Diagrams
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private INotificationRepository repository;

    @Mock
    private INotificationSender sender;

    private NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService(repository, sender);
    }

    @Test
    @DisplayName("[Notification State: PENDING->SENT] Sender delivers -> transitions to SENT")
    void dispatchNotification_senderSucceeds_transitionsToSent() {
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sender.send(any(Notification.class))).thenReturn(true);

        Notification result = service.dispatchNotification("user-1", Notification.Channel.PUSH, "Bin 90% full");

        assertEquals(Notification.NotificationStatus.SENT, result.getStatus());
        verify(repository, times(2)).save(any(Notification.class));
        verify(sender).send(any(Notification.class));
    }

    @Test
    @DisplayName("[Notification State: PENDING->FAILED] Sender fails -> transitions to FAILED")
    void dispatchNotification_senderFails_transitionsToFailed() {
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sender.send(any(Notification.class))).thenReturn(false);

        Notification result = service.dispatchNotification("user-1", Notification.Channel.EMAIL, "Reward credited");

        assertEquals(Notification.NotificationStatus.FAILED, result.getStatus());
        verify(repository, times(2)).save(any(Notification.class));
        verify(sender).send(any(Notification.class));
    }
}
