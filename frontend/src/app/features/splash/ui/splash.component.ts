import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { MailboxUiConfigService } from '../../../core/config/mailbox-ui-config.service';

@Component({
  selector: 'app-splash',
  standalone: true,
  templateUrl: './splash.component.html',
  styleUrl: './splash.component.scss'
})
export class SplashComponent {
  constructor(router: Router, config: MailboxUiConfigService) {
    config.getSplashDurationMs().subscribe((durationMs) => {
      setTimeout(() => router.navigateByUrl('/startup'), durationMs);
    });
  }
}
