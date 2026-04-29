import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { LoginUseCase } from '../../../core/auth/application/login.usecase';

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

  constructor(
    private loginUseCase: LoginUseCase,
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
}
