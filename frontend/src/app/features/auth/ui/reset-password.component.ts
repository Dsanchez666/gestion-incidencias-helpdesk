import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ResetPasswordUseCase } from '../../../core/auth/application/reset-password.usecase';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [FormsModule, NgIf],
  templateUrl: './reset-password.component.html',
  styleUrl: './login.component.scss'
})
export class ResetPasswordComponent {
  token = '';
  newPassword = '';
  message = '';
  error = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private resetPasswordUseCase: ResetPasswordUseCase
  ) {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
  }

  submit(): void {
    this.error = '';
    this.message = '';
    this.resetPasswordUseCase.execute(this.token, this.newPassword).subscribe({
      next: () => {
        this.message = 'Contraseña restablecida correctamente.';
        setTimeout(() => this.router.navigateByUrl('/login'), 800);
      },
      error: () => (this.error = 'Token inválido o expirado.')
    });
  }
}

