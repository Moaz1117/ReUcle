import { IsBoolean, IsNotEmpty, IsString } from 'class-validator';

export class SetPreferenceDto {
  @IsString()
  @IsNotEmpty()
  userId!: string;

  @IsString()
  @IsNotEmpty()
  category!: string;

  @IsBoolean()
  email!: boolean;

  @IsBoolean()
  sms!: boolean;

  @IsBoolean()
  inApp!: boolean;
}

export class SendNotificationDto {
  @IsString()
  @IsNotEmpty()
  userId!: string;

  @IsString()
  @IsNotEmpty()
  category!: string;

  @IsString()
  @IsNotEmpty()
  title!: string;

  @IsString()
  @IsNotEmpty()
  body!: string;
}
