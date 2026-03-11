import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgIf } from '@angular/common';
import { AuthService } from '../../../core/auth/auth.service';
import { EntraMockService } from '../../../core/auth/entra-mock.service';

@Component({
  selector: 'app-startup',
  standalone: true,
  imports: [NgIf],
  templateUrl: './startup.component.html',
  styleUrl: './startup.component.scss'
})
export class StartupComponent {
  statusMessage = 'Conectando con Entra ID...';
  hasError = false;
  tokenUsed = '';
  private readonly minDelayMs = 5000;
  private readonly startedAt = Date.now();

  constructor(
    private entraMockService: EntraMockService,
    private authService: AuthService,
    private router: Router
  ) {
    this.authService.logout();

    this.entraMockService.validateToken().subscribe({
      next: (response) => {
        this.tokenUsed = response.accessToken || '';
        if (response.result === 'valid_token' && response.accessToken) {
          this.authService.setToken(response.accessToken);
          this.delayThenNavigate('/buzones');
          return;
        }

        this.hasError = true;
        if (response.result === 'invalid_token') {
          this.statusMessage = response.error || 'Token Entra ID invalido.';
        } else {
          this.statusMessage = response.error || 'Error validando token Entra ID.';
        }
        this.delayThenNavigate('/login');
      },
      error: () => {
        this.hasError = true;
        this.statusMessage = 'No se pudo validar el token Entra ID.';
        this.delayThenNavigate('/login');
      }
    });
  }

  private delayThenNavigate(path: string): void {
    const elapsed = Date.now() - this.startedAt;
    const remaining = Math.max(this.minDelayMs - elapsed, 0);
    setTimeout(() => this.router.navigateByUrl(path), remaining);
  }
}
