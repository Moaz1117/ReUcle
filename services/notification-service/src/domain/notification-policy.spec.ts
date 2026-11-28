import { NotificationPolicy, NotificationPreference } from './notification-policy';

describe('NotificationPolicy', () => {
  it('uses in-app only when no preference is saved (US70 AC2)', () => {
    expect(NotificationPolicy.resolveChannels(null)).toEqual(['IN_APP']);
  });

  it('skips channels that are turned off (US70 AC1)', () => {
    const pref = new NotificationPreference('u1', 'REWARD', false, true, true);
    expect(NotificationPolicy.resolveChannels(pref)).toEqual(['SMS', 'IN_APP']);
  });

  it('retries up to 3 times then stops (US72 AC1)', () => {
    expect(NotificationPolicy.shouldRetry(1)).toBe(true);
    expect(NotificationPolicy.shouldRetry(3)).toBe(true);
    expect(NotificationPolicy.shouldRetry(4)).toBe(false);
  });
});
