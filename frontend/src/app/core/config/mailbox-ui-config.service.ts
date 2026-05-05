import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, shareReplay } from 'rxjs';

interface MailboxUiConfig {
  ui?: {
    mailPreviewLength?: number;
    splashDurationSeconds?: number;
  };
}

@Injectable({ providedIn: 'root' })
export class MailboxUiConfigService {
  private readonly config$: Observable<MailboxUiConfig>;

  constructor(private http: HttpClient) {
    this.config$ = this.http.get<MailboxUiConfig>('assets/Mailboxes_Conf.json').pipe(shareReplay(1));
  }

  getPreviewLength(): Observable<number> {
    return this.config$.pipe(map((cfg) => cfg.ui?.mailPreviewLength ?? 50));
  }

  getSplashDurationMs(): Observable<number> {
    return this.config$.pipe(map((cfg) => (cfg.ui?.splashDurationSeconds ?? 3) * 1000));
  }
}
