import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { DataSource, Repository } from 'typeorm';
import { BinRepository, NewBin } from '../../domain/bin-repository';
import { BinStatus, SmartBin } from '../../domain/smart-bin';
import { BinReadingOrm, SmartBinOrm } from './orm-entities';

function toDomain(row: SmartBinOrm): SmartBin {
  return new SmartBin(
    row.binId,
    row.ownerUserId,
    row.deviceId,
    row.address,
    row.thresholdPct,
    row.status as BinStatus,
    row.lastReadingAt,
  );
}

@Injectable()
export class TypeOrmBinRepository implements BinRepository {
  constructor(
    @InjectRepository(SmartBinOrm) private readonly bins: Repository<SmartBinOrm>,
    private readonly dataSource: DataSource,
  ) {}

  async createBin(input: NewBin): Promise<SmartBin> {
    const row = await this.bins.save(
      this.bins.create({ ...input, status: 'NORMAL', lastReadingAt: null }),
    );
    return toDomain(row);
  }

  async findById(id: string): Promise<SmartBin | null> {
    const row = await this.bins.findOne({ where: { binId: id } });
    return row ? toDomain(row) : null;
  }

  async findNeedingCollection(): Promise<SmartBin[]> {
    const rows = await this.bins.find({ where: { status: 'NEEDS_COLLECTION' } });
    return rows.map(toDomain);
  }

  async recordReading(bin: SmartBin, fillPct: number, recordedAt: Date): Promise<void> {
    await this.dataSource.transaction(async (em) => {
      await em.save(BinReadingOrm, em.create(BinReadingOrm, { binId: bin.id, fillPct, recordedAt }));
      await em.update(SmartBinOrm, { binId: bin.id }, {
        status: bin.status,
        lastReadingAt: bin.lastReadingAt,
      });
    });
  }
}
