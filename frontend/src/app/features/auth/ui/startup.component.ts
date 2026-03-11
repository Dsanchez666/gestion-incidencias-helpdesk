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

  constructor(
    private entraMockService: EntraMockService,
    private authService: AuthService,
    private router: Router
  ) {
    this.authService.logout();

    this.entraMockService.validateToken().subscribe({
      next: (response) => {
        if (response.success && response.accessToken) {
          this.authService.setToken(response.accessToken);
          this.router.navigateByUrl('/buzones');
          return;
        }

        this.hasError = true;
        this.statusMessage = response.error || 'No se pudo validar el token Entra ID.';
        setTimeout(() => this.router.navigateByUrl('/login'), 400);
      },
      error: () => {
        this.hasError = true;
        this.statusMessage = 'No se pudo validar el token Entra ID.';
        setTimeout(() => this.router.navigateByUrl('/login'), 400);
      }
    });
  }
}
