import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { BinController } from './api/bin.controller';
import { HealthController } from './api/health.controller';
import { BinService } from './application/bin.service';
import { BIN_REPOSITORY } from './domain/bin-repository';
import { BinReadingOrm, SmartBinOrm } from './infrastructure/persistence/orm-entities';
import { TypeOrmBinRepository } from './infrastructure/persistence/typeorm-bin.repository';

@Module({
  imports: [
    TypeOrmModule.forRoot({
      type: 'postgres',
      host: process.env.DB_HOST ?? 'localhost',
      port: Number(process.env.DB_PORT ?? 5432),
      username: process.env.DB_USER,
      password: process.env.DB_PASSWORD,
      database: process.env.DB_NAME,
      entities: [SmartBinOrm, BinReadingOrm],
      // auto-creates tables from the entities: fine for PM1/dev, replace with migrations later
      synchronize: true,
      retryAttempts: 10,
      retryDelay: 3000,
    }),
    TypeOrmModule.forFeature([SmartBinOrm, BinReadingOrm]),
  ],
  controllers: [HealthController, BinController],
  providers: [BinService, { provide: BIN_REPOSITORY, useClass: TypeOrmBinRepository }],
})
export class AppModule {}
