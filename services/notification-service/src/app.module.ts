import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { HealthController } from './api/health.controller';
import { NotificationController } from './api/notification.controller';
import { NotificationService } from './application/notification.service';
import { MESSAGING_GATEWAY } from './domain/messaging-gateway';
import { NOTIFICATION_REPOSITORY } from './domain/notification-repository';
import { SimulatedMessagingGateway } from './infrastructure/clients/simulated-messaging-gateway';
import {
  DeliveryAttemptOrm,
  NotificationOrm,
  NotificationPreferenceOrm,
} from './infrastructure/persistence/orm-entities';
import { TypeOrmNotificationRepository } from './infrastructure/persistence/typeorm-notification.repository';

@Module({
  imports: [
    TypeOrmModule.forRoot({
      type: 'postgres',
      host: process.env.DB_HOST ?? 'localhost',
      port: Number(process.env.DB_PORT ?? 5432),
      username: process.env.DB_USER,
      password: process.env.DB_PASSWORD,
      database: process.env.DB_NAME,
      entities: [NotificationOrm, NotificationPreferenceOrm, DeliveryAttemptOrm],
      // auto-creates tables from the entities: fine for PM1/dev, replace with migrations later
      synchronize: true,
      retryAttempts: 10,
      retryDelay: 3000,
    }),
    TypeOrmModule.forFeature([NotificationOrm, NotificationPreferenceOrm, DeliveryAttemptOrm]),
  ],
  controllers: [HealthController, NotificationController],
  providers: [
    NotificationService,
    { provide: NOTIFICATION_REPOSITORY, useClass: TypeOrmNotificationRepository },
    { provide: MESSAGING_GATEWAY, useClass: SimulatedMessagingGateway },
  ],
})
export class AppModule {}
