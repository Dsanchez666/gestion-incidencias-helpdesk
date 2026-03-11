import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, defer, from, map, of, switchMap } from 'rxjs';

interface EntraIdConfig {
  tenantId?: string;
  clientId?: string;
  authorityUrl?: string;
  pingUrl?: string;
}

@Injectable({ providedIn: 'root' })
export class EntraIdService {
  private readonly configUrl = 'assets/EntraID_Conf.json';

  constructor(private http: HttpClient) {}

  testConnection() {
    return this.http.get<EntraIdConfig>(this.configUrl).pipe(
      switchMap((config) => defer(() => from(this.ping(config)))),
      catchError(() => of(false))
    );
  }

  private resolvePingUrl(config: EntraIdConfig): string {
    if (config.pingUrl) {
      return config.pingUrl;
    }

    if (config.authorityUrl) {
      return `${config.authorityUrl.replace(/\/$/, '')}/.well-known/openid-configuration`;
    }

    if (config.tenantId) {
      return `https://login.microsoftonline.com/${config.tenantId}/v2.0/.well-known/openid-configuration`;
    }

    return 'https://login.microsoftonline.com/common/v2.0/.well-known/openid-configuration';
  }

  private async ping(config: EntraIdConfig): Promise<boolean> {
    const url = this.resolvePingUrl(config);
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 3000);

    try {
      await fetch(url, { method: 'GET', mode: 'no-cors', signal: controller.signal });
      clearTimeout(timeoutId);
      return true;
    } catch {
      clearTimeout(timeoutId);
      return false;
    }
  }
}
