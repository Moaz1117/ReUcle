import { Channel, NotificationPreference } from './notification-policy';

export interface DeliveryLog {
  notificationId: string;
  channel: Channel;
  attemptNo: number;
  status: 'SENT' | 'FAILED';
  providerReference?: string;
  errorMessage?: string;
}

export interface NotificationRepository {
  savePreference(pref: NotificationPreference): Promise<void>;
  findPreference(userId: string, category: string): Promise<NotificationPreference | null>;
  createNotification(recipientUserId: string, category: string, title: string, body: string): Promise<string>;
  logAttempt(log: DeliveryLog): Promise<void>;
  countAttempts(notificationId: string, channel: Channel): Promise<number>;
}

export const NOTIFICATION_REPOSITORY = Symbol('NOTIFICATION_REPOSITORY');
