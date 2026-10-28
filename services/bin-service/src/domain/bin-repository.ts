import { SmartBin } from './smart-bin';

export interface NewBin {
  ownerUserId: string;
  deviceId: string;
  address: string;
  thresholdPct: number;
}

/** Abstraction the application layer depends on (DIP). Implemented in infrastructure/persistence. */
export interface BinRepository {
  createBin(input: NewBin): Promise<SmartBin>;
  findById(id: string): Promise<SmartBin | null>;
  findNeedingCollection(): Promise<SmartBin[]>;
  /** Stores the reading and the updated bin state in one transaction. */
  recordReading(bin: SmartBin, fillPct: number, recordedAt: Date): Promise<void>;
}

export const BIN_REPOSITORY = Symbol('BIN_REPOSITORY');
