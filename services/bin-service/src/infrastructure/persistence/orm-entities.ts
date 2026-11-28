import { Column, Entity, JoinColumn, ManyToOne, PrimaryGeneratedColumn } from 'typeorm';

/** Table SmartBins from Section 6 (ERD). */
@Entity({ name: 'smart_bins' })
export class SmartBinOrm {
  @PrimaryGeneratedColumn('uuid', { name: 'bin_id' })
  binId: string;

  // external reference to user-service (no cross-database FK)
  @Column({ name: 'owner_user_id', type: 'varchar' })
  ownerUserId: string;

  @Column({ name: 'device_id', type: 'varchar', unique: true })
  deviceId: string;

  @Column({ type: 'varchar' })
  address: string;

  @Column({ name: 'threshold_pct', type: 'int', default: 80 })
  thresholdPct: number;

  @Column({ type: 'varchar', default: 'NORMAL' })
  status: string;

  @Column({ name: 'last_reading_at', type: 'timestamptz', nullable: true })
  lastReadingAt: Date | null;
}

/** Table BinReadings from Section 6 (ERD). */
@Entity({ name: 'bin_readings' })
export class BinReadingOrm {
  @PrimaryGeneratedColumn('uuid', { name: 'reading_id' })
  readingId: string;

  @Column({ name: 'bin_id', type: 'uuid' })
  binId: string;

  @ManyToOne(() => SmartBinOrm, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'bin_id' })
  bin: SmartBinOrm;

  @Column({ name: 'fill_pct', type: 'int' })
  fillPct: number;

  @Column({ name: 'recorded_at', type: 'timestamptz' })
  recordedAt: Date;
}
