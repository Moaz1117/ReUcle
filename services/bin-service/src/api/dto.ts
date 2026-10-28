import { IsDateString, IsInt, IsNotEmpty, IsOptional, IsString, Max, Min } from 'class-validator';

export class RegisterBinDto {
  @IsString() @IsNotEmpty() ownerUserId: string;
  @IsString() @IsNotEmpty() deviceId: string;
  @IsString() @IsNotEmpty() address: string;
  @IsOptional() @IsInt() @Min(1) @Max(100) thresholdPct?: number;
}

export class ReadingDto {
  @IsInt() @Min(0) @Max(100) fillPct: number;
  @IsOptional() @IsDateString() recordedAt?: string;
}
