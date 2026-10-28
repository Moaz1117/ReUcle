import { Inject, Injectable } from '@nestjs/common';
import { MESSAGING_GATEWAY, MessagingGateway } from '../domain/messaging-gateway';
import { NOTIFICATION_REPOSITORY, NotificationRepository } from '../domain/notification-repository';
import { Channel, NotificationPolicy, NotificationPreference } from '../domain/notification-policy';

export interface ChannelResult {
  channel: Channel;
  status: 'SENT' | 'FAILED';
  attempts: number;
}

@Injectable()
export class NotificationService {
  constructor(
    @Inject(NOTIFICATION_REPOSITORY) private readonly repo: NotificationRepository,
    @Inject(MESSAGING_GATEWAY) private readonly gateway: MessagingGateway,
  ) {}

  // US70: save the channels a user wants for a notification category
  async setPreference(userId: string, category: string, email: boolean, sms: boolean, inApp: boolean) {
    await this.repo.savePreference(new NotificationPreference(userId, category, email, sms, inApp));
  }

  async getPreference(userId: string, category: string) {
    return this.repo.findPreference(userId, category);
  }

  // US70 + US72: pick channels from preferences, deliver, retry failures, log every attempt
  async send(userId: string, category: string, title: string, body: string) {
    const pref = await this.repo.findPreference(userId, category);
    const channels = NotificationPolicy.resolveChannels(pref);
    const notificationId = await this.repo.createNotification(userId, category, title, body);

    const results: ChannelResult[] = [];
    for (const channel of channels) {
      let attemptNo = 1;
      let status: 'SENT' | 'FAILED' = 'FAILED';
      for (;;) {
        try {
          const ref = await this.gateway.deliver(channel, userId, title, body);
          await this.repo.logAttempt({ notificationId, channel, attemptNo, status: 'SENT', providerReference: ref });
          status = 'SENT';
          break;
        } catch (e) {
          await this.repo.logAttempt({
            notificationId, channel, attemptNo, status: 'FAILED', errorMessage: (e as Error).message,
          });
          if (!NotificationPolicy.shouldRetry(attemptNo)) break;
          attemptNo++;
        }
      }
      results.push({ channel, status, attempts: attemptNo });
    }
    return { notificationId, results };
  }
}
