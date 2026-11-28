import { Channel } from './notification-policy';

export interface MessagingGateway {
  // resolves with a provider reference, throws on failure
  deliver(channel: Channel, userId: string, title: string, body: string): Promise<string>;
}

export const MESSAGING_GATEWAY = Symbol('MESSAGING_GATEWAY');
