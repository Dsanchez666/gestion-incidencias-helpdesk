import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgIf } from '@angular/common';
import { AuthService } from '../../../core/auth/auth.service';
import { EntraAppService } from '../../../core/auth/entra-app.service';

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
    private entraAppService: EntraAppService,
    private authService: AuthService,
    private router: Router
  ) {
    this.authService.logout();
    console.info('Startup: solicitando token app-to-app en Entra ID');

    this.entraAppService.getAppToken().subscribe({
      next: (response) => {
        console.info('Startup: respuesta app-token', response);
        this.tokenUsed = response.accessToken || '';
        if (response.success && response.accessToken) {
          console.info('Startup: token recibido, guardando token y navegando a /buzones');
          this.authService.setToken(response.accessToken);
          this.delayThenNavigate('/buzones');
          return;
        }

        this.hasError = true;
        this.statusMessage = response.error || 'No se pudo obtener token de Entra ID.';
        console.warn('Startup: fallo obteniendo token, navegando a /login');
        this.delayThenNavigate('/login');
      },
      error: () => {
        this.hasError = true;
        this.statusMessage = 'No se pudo obtener token de Entra ID.';
        console.error('Startup: error de red en app-token, navegando a /login');
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
