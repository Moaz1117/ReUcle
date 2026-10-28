export type BinStatus = 'NORMAL' | 'NEEDS_COLLECTION';

export const DEFAULT_THRESHOLD_PCT = 80;

/**
 * Domain object: pure business rules, no framework or database code.
 * Maps to: SmartBins (Section 6) and FR3 "Monitor Bin Status".
 */
export class SmartBin {
  constructor(
    public readonly id: string,
    public readonly ownerUserId: string,
    public readonly deviceId: string,
    public readonly address: string,
    public thresholdPct: number = DEFAULT_THRESHOLD_PCT,
    public status: BinStatus = 'NORMAL',
    public lastReadingAt: Date | null = null,
  ) {}

  /** FR3.2: flag the bin when its fill level reaches the configured threshold. */
  applyReading(fillPct: number, recordedAt: Date): void {
    if (!Number.isFinite(fillPct) || fillPct < 0 || fillPct > 100) {
      throw new RangeError('fillPct must be between 0 and 100');
    }
    this.lastReadingAt = recordedAt;
    this.status = fillPct >= this.thresholdPct ? 'NEEDS_COLLECTION' : 'NORMAL';
  }
}
