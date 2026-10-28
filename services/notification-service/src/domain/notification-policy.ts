export type Channel = 'EMAIL' | 'SMS' | 'IN_APP';

// US72 / FR24.4: retry a failed delivery up to 3 times
export const MAX_RETRIES = 3;

export class NotificationPreference {
  constructor(
    public readonly userId: string,
    public readonly category: string,
    public readonly emailEnabled: boolean,
    public readonly smsEnabled: boolean,
    public readonly inAppEnabled: boolean,
  ) {}
}

export class NotificationPolicy {
  // US70 AC2: no saved preference -> in-app only
  // US70 AC1: a channel turned off is never used
  static resolveChannels(pref: NotificationPreference | null): Channel[] {
    if (!pref) return ['IN_APP'];
    const channels: Channel[] = [];
    if (pref.emailEnabled) channels.push('EMAIL');
    if (pref.smsEnabled) channels.push('SMS');
    if (pref.inAppEnabled) channels.push('IN_APP');
    return channels;
  }

  // attemptsMade includes the first try, so 1 try + 3 retries = 4 attempts
  static shouldRetry(attemptsMade: number): boolean {
    return attemptsMade >= 1 && attemptsMade <= MAX_RETRIES;
  }
}
