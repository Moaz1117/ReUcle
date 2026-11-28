import { Body, Controller, Get, Param, ParseUUIDPipe, Post } from '@nestjs/common';
import { BinService } from '../application/bin.service';
import { ReadingDto, RegisterBinDto } from './dto';

@Controller('bins')
export class BinController {
  constructor(private readonly service: BinService) {}

  @Post()
  register(@Body() dto: RegisterBinDto) {
    return this.service.registerBin(dto);
  }

  // must be declared before ':id'
  @Get('needing-collection')
  needingCollection() {
    return this.service.getBinsNeedingCollection();
  }

  @Get(':id')
  get(@Param('id', ParseUUIDPipe) id: string) {
    return this.service.getBinStatus(id);
  }

  @Post(':id/readings')
  reading(@Param('id', ParseUUIDPipe) id: string, @Body() dto: ReadingDto) {
    return this.service.receiveReading(id, dto.fillPct, dto.recordedAt ? new Date(dto.recordedAt) : undefined);
  }
}
