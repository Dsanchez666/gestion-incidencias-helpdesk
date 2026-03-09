import { enableProdMode, importProvidersFrom } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AppComponent } from './app/app.component';
import { IncidenciaFormComponent } from './app/features/incidencia/ui/incidencia-form.component';
import { LoginComponent } from './app/features/auth/ui/login.component';
import { MailboxListComponent } from './app/features/mailbox/ui/mailbox-list.component';
import { authGuard } from './app/core/auth/auth.guard';
import { authInterceptor } from './app/core/auth/auth.interceptor';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: LoginComponent },
  { path: 'buzones', component: MailboxListComponent, canActivate: [authGuard] },
  { path: 'incidencias', component: IncidenciaFormComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: 'login' }
];

bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(withInterceptors([authInterceptor])),
    provideRouter(routes),
    importProvidersFrom(FormsModule)
  ]
}).catch((err) => console.error(err));
