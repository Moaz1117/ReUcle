import { Body, Controller, Get, NotFoundException, Param, Post, Put } from '@nestjs/common';
import { NotificationService } from '../application/notification.service';
import { SendNotificationDto, SetPreferenceDto } from './dto';

@Controller()
export class NotificationController {
  constructor(private readonly service: NotificationService) {}

  // US70: set notification channel preferences
  @Put('preferences')
  async setPreference(@Body() dto: SetPreferenceDto) {
    await this.service.setPreference(dto.userId, dto.category, dto.email, dto.sms, dto.inApp);
    return { userId: dto.userId, category: dto.category, email: dto.email, sms: dto.sms, inApp: dto.inApp };
  }

  @Get('preferences/:userId/:category')
  async getPreference(@Param('userId') userId: string, @Param('category') category: string) {
    const pref = await this.service.getPreference(userId, category);
    if (!pref) throw new NotFoundException('No preference saved for this user and category');
    return pref;
  }

  // US70 + US72: send, retry failed channels, log every attempt
  @Post('notifications')
  async send(@Body() dto: SendNotificationDto) {
    return this.service.send(dto.userId, dto.category, dto.title, dto.body);
  }
}
