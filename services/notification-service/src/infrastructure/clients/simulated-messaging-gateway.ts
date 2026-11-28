import { Injectable } from '@nestjs/common';
import { MessagingGateway } from '../../domain/messaging-gateway';
import { Channel } from '../../domain/notification-policy';

// Simulated gateway (test mode). A title starting with "FAIL" makes EMAIL/SMS fail,
// so the retry rule can be demonstrated.
@Injectable()
export class SimulatedMessagingGateway implements MessagingGateway {
  async deliver(channel: Channel, userId: string, title: string): Promise<string> {
    if (channel !== 'IN_APP' && title.startsWith('FAIL')) {
      throw new Error('simulated gateway failure');
    }
    return `sim-${channel.toLowerCase()}-${Date.now()}`;
  }
}
