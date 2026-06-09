import { enableProdMode, importProvidersFrom } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AppComponent } from './app/app.component';
import { IncidenciaFormComponent } from './app/features/incidencia/ui/incidencia-form.component';
import { LoginComponent } from './app/features/auth/ui/login.component';
import { AuthSelectComponent } from './app/features/auth/ui/auth-select.component';
import { StartupComponent } from './app/features/auth/ui/startup.component';
import { ResetPasswordComponent } from './app/features/auth/ui/reset-password.component';
import { MailboxListComponent } from './app/features/mailbox/ui/mailbox-list.component';
import { SplashComponent } from './app/features/splash/ui/splash.component';
import { InboxComponent } from './app/features/inbox/ui/inbox.component';
import { authGuard } from './app/core/auth/auth.guard';
import { authInterceptor } from './app/core/auth/auth.interceptor';
import { AuthService } from './app/core/auth/auth.service';
import { EntraAppService } from './app/core/auth/entra-app.service';
import { EntraUserService } from './app/core/auth/entra-user.service';
import { AUTH_SESSION_PORT } from './app/core/auth/application/port/out/auth-session.port';
import { ENTRA_APP_TOKEN_PORT } from './app/core/auth/application/port/out/entra-app-token.port';
import { ENTRA_USER_TOKEN_PORT } from './app/core/auth/application/port/out/entra-user-token.port';
import { IncidenciaApiService } from './app/features/incidencia/infrastructure/incidencia-api.service';
import { INCIDENCIA_API_PORT } from './app/features/incidencia/application/port/out/incidencia-api.port';
import { MailboxApiService } from './app/features/mailbox/infrastructure/mailbox-api.service';
import { MAILBOX_API_PORT } from './app/features/mailbox/application/port/out/mailbox-api.port';

const routes: Routes = [
  { path: '', component: SplashComponent },
  { path: 'auth-select', component: AuthSelectComponent },
  { path: 'startup', component: StartupComponent },
  { path: 'login', component: LoginComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'buzones', component: MailboxListComponent, canActivate: [authGuard] },
  { path: 'inbox', component: InboxComponent, canActivate: [authGuard] },
  { path: 'incidencias', component: IncidenciaFormComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];

bootstrapApplication(AppComponent, {
  providers: [
    { provide: AUTH_SESSION_PORT, useExisting: AuthService },
    { provide: ENTRA_APP_TOKEN_PORT, useExisting: EntraAppService },
    { provide: ENTRA_USER_TOKEN_PORT, useExisting: EntraUserService },
    { provide: INCIDENCIA_API_PORT, useExisting: IncidenciaApiService },
    { provide: MAILBOX_API_PORT, useExisting: MailboxApiService },
    provideHttpClient(withInterceptors([authInterceptor])),
    provideRouter(routes),
    importProvidersFrom(FormsModule)
  ]
}).catch((err) => console.error(err));
