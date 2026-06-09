import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-auth-select',
  standalone: true,
  templateUrl: './auth-select.component.html',
  styleUrl: './auth-select.component.scss'
})
export class AuthSelectComponent {
  constructor(private router: Router) {}

  loginWithEntra(): void {
    this.router.navigateByUrl('/startup');
  }

  loginWithCredentials(): void {
    this.router.navigateByUrl('/login');
  }
}
