import { Column, CreateDateColumn, Entity, Index, PrimaryGeneratedColumn } from 'typeorm';

@Entity('notifications')
export class NotificationOrm {
  @PrimaryGeneratedColumn('uuid', { name: 'notification_id' }) id!: string;
  @Column({ name: 'recipient_user_id' }) recipientUserId!: string;
  @Column() category!: string;
  @Column() title!: string;
  @Column('text') body!: string;
  @Column({ name: 'read_at', type: 'timestamptz', nullable: true }) readAt!: Date | null;
  @CreateDateColumn({ name: 'created_at' }) createdAt!: Date;
}

@Entity('notification_preferences')
@Index(['userId', 'category'], { unique: true })
export class NotificationPreferenceOrm {
  @PrimaryGeneratedColumn('uuid', { name: 'preference_id' }) id!: string;
  @Column({ name: 'user_id' }) userId!: string;
  @Column() category!: string;
  @Column({ name: 'email_enabled', default: false }) emailEnabled!: boolean;
  @Column({ name: 'sms_enabled', default: false }) smsEnabled!: boolean;
  @Column({ name: 'in_app_enabled', default: true }) inAppEnabled!: boolean;
}

@Entity('delivery_attempts')
export class DeliveryAttemptOrm {
  @PrimaryGeneratedColumn('uuid', { name: 'attempt_id' }) id!: string;
  @Column({ name: 'notification_id' }) notificationId!: string;
  @Column() channel!: string;
  @Column({ name: 'attempt_no', type: 'int' }) attemptNo!: number;
  @Column() status!: string;
  @Column({ name: 'provider_reference', type: 'varchar', nullable: true }) providerReference!: string | null;
  @CreateDateColumn({ name: 'attempted_at' }) attemptedAt!: Date;
  @Column({ name: 'error_message', type: 'text', nullable: true }) errorMessage!: string | null;
}
