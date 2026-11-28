import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { DeliveryLog, NotificationRepository } from '../../domain/notification-repository';
import { Channel, NotificationPreference } from '../../domain/notification-policy';
import { DeliveryAttemptOrm, NotificationOrm, NotificationPreferenceOrm } from './orm-entities';

@Injectable()
export class TypeOrmNotificationRepository implements NotificationRepository {
  constructor(
    @InjectRepository(NotificationOrm) private readonly notifications: Repository<NotificationOrm>,
    @InjectRepository(NotificationPreferenceOrm) private readonly prefs: Repository<NotificationPreferenceOrm>,
    @InjectRepository(DeliveryAttemptOrm) private readonly attempts: Repository<DeliveryAttemptOrm>,
  ) {}

  async savePreference(p: NotificationPreference): Promise<void> {
    const existing = await this.prefs.findOne({ where: { userId: p.userId, category: p.category } });
    await this.prefs.save({
      ...(existing ?? {}),
      userId: p.userId,
      category: p.category,
      emailEnabled: p.emailEnabled,
      smsEnabled: p.smsEnabled,
      inAppEnabled: p.inAppEnabled,
    });
  }

  async findPreference(userId: string, category: string): Promise<NotificationPreference | null> {
    const r = await this.prefs.findOne({ where: { userId, category } });
    return r ? new NotificationPreference(r.userId, r.category, r.emailEnabled, r.smsEnabled, r.inAppEnabled) : null;
  }

  async createNotification(recipientUserId: string, category: string, title: string, body: string): Promise<string> {
    const saved = await this.notifications.save({ recipientUserId, category, title, body, readAt: null });
    return saved.id;
  }

  async logAttempt(log: DeliveryLog): Promise<void> {
    await this.attempts.save({
      notificationId: log.notificationId,
      channel: log.channel,
      attemptNo: log.attemptNo,
      status: log.status,
      providerReference: log.providerReference ?? null,
      errorMessage: log.errorMessage ?? null,
    });
  }

  countAttempts(notificationId: string, channel: Channel): Promise<number> {
    return this.attempts.count({ where: { notificationId, channel } });
  }
}
