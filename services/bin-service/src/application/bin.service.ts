import { Inject, Injectable, NotFoundException } from '@nestjs/common';
import { BIN_REPOSITORY, BinRepository } from '../domain/bin-repository';
import { DEFAULT_THRESHOLD_PCT, SmartBin } from '../domain/smart-bin';

/** Use-cases of the provided interface IBin. */
@Injectable()
export class BinService {
  constructor(@Inject(BIN_REPOSITORY) private readonly repo: BinRepository) {}

  /** registerBin (FR17.3) */
  registerBin(input: { ownerUserId: string; deviceId: string; address: string; thresholdPct?: number }) {
    return this.repo.createBin({
      ownerUserId: input.ownerUserId,
      deviceId: input.deviceId,
      address: input.address,
      thresholdPct: input.thresholdPct ?? DEFAULT_THRESHOLD_PCT,
    });
  }

  /** getBinStatus (FR3.1, FR3.3) */
  async getBinStatus(id: string): Promise<SmartBin> {
    const bin = await this.repo.findById(id);
    if (!bin) throw new NotFoundException(`Bin ${id} not found`);
    return bin;
  }

  /** getBinsNeedingCollection (FR3.2) */
  getBinsNeedingCollection() {
    return this.repo.findNeedingCollection();
  }

  /** receiveReading (FR3.1, FR3.2) */
  async receiveReading(id: string, fillPct: number, recordedAt: Date = new Date()): Promise<SmartBin> {
    const bin = await this.getBinStatus(id);
    bin.applyReading(fillPct, recordedAt);
    await this.repo.recordReading(bin, fillPct, recordedAt);
    return bin;
  }
}
