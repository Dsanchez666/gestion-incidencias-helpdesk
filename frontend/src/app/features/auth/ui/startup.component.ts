import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgIf } from '@angular/common';
import { AuthService } from '../../../core/auth/auth.service';
import { EntraIdService } from '../../../core/auth/entra-id.service';

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
    private entraIdService: EntraIdService,
    private authService: AuthService,
    private router: Router
  ) {
    if (this.authService.isAuthenticated()) {
      this.router.navigateByUrl('/buzones');
      return;
    }

    this.entraIdService.testConnection().subscribe((connected) => {
      if (connected) {
        this.authService.startEntraSession();
        this.router.navigateByUrl('/buzones');
        return;
      }

      this.hasError = true;
      this.statusMessage = 'No se pudo conectar con Entra ID. Mostrando login.';
      setTimeout(() => this.router.navigateByUrl('/login'), 400);
    });
  }
}
