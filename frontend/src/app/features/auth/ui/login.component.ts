import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { LoginUseCase } from '../../../core/auth/application/login.usecase';
import { RecoverPasswordUseCase } from '../../../core/auth/application/recover-password.usecase';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, NgIf],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  username = '';
  password = '';
  error = '';
  isLoading = false;
  recoverUserOrEmail = '';
  recoverMessage = '';

  constructor(
    private loginUseCase: LoginUseCase,
    private recoverPasswordUseCase: RecoverPasswordUseCase,
    private router: Router
  ) {}

  submit(): void {
    this.error = '';
    this.isLoading = true;

    this.loginUseCase.execute(this.username, this.password).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigateByUrl('/buzones');
      },
      error: () => {
        this.isLoading = false;
        this.error = 'Usuario no existe o la clave es incorrecta';
      }
    });
  }

  loginConEntra(): void {
    this.router.navigateByUrl('/startup');
  }

  recoverPassword(): void {
    this.error = '';
    this.recoverMessage = '';
    this.recoverPasswordUseCase.execute(this.recoverUserOrEmail).subscribe({
      next: () => (this.recoverMessage = 'Si el usuario existe, se ha enviado un enlace de recuperación.'),
      error: () => (this.error = 'No se pudo iniciar recuperación de contraseña')
    });
  }
}
