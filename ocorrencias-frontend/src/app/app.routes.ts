import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent) },
  {
    path: '',
    loadComponent: () => import('./layout/shell/shell.component').then(m => m.ShellComponent),
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'ocorrencias', loadComponent: () => import('./pages/ocorrencias/lista/lista.component').then(m => m.ListaComponent) },
      { path: 'ocorrencias/nova', loadComponent: () => import('./pages/ocorrencias/nova/nova.component').then(m => m.NovaComponent) },
      { path: 'ocorrencias/:id', loadComponent: () => import('./pages/ocorrencias/detalhe/detalhe.component').then(m => m.DetalheComponent) },
      { path: 'clientes', loadComponent: () => import('./pages/clientes/lista/lista.component').then(m => m.ListaComponent) },
    ]
  },
  { path: '**', redirectTo: '' }
];
