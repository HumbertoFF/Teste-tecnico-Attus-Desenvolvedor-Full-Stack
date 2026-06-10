import { Component, inject } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../core/services/auth.service';

const DEFAULT_EMAIL = 'admin' + '@' + 'admin.com.br';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule,
            MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <div class="login-page">
      <div class="login-card">

        <div class="login-brand">
          <div class="brand-mark">OC</div>
          <h1 class="brand-title">Ocorrências</h1>
          <p class="brand-sub">Sistema de Gestão e Rastreamento</p>
        </div>

        @if (expired) {
          <div class="banner banner-warn">
            <mat-icon>timer_off</mat-icon>
            Sua sessão expirou. Faça login novamente.
          </div>
        }

        <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
          <mat-form-field appearance="fill" class="fw">
            <mat-label>E-mail</mat-label>
            <mat-icon matPrefix class="field-icon">email</mat-icon>
            <input matInput type="email" formControlName="email" autocomplete="email" />
            @if (form.get('email')?.hasError('required') && form.get('email')?.touched) {
              <mat-error>E-mail obrigatório</mat-error>
            }
            @if (form.get('email')?.hasError('email') && form.get('email')?.touched) {
              <mat-error>E-mail inválido</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="fill" class="fw">
            <mat-label>Senha</mat-label>
            <mat-icon matPrefix class="field-icon">lock_outline</mat-icon>
            <input matInput [type]="showPass ? 'text' : 'password'"
                   formControlName="senha" autocomplete="current-password" />
            <button mat-icon-button matSuffix type="button" (click)="showPass = !showPass">
              <mat-icon>{{ showPass ? 'visibility_off' : 'visibility' }}</mat-icon>
            </button>
            @if (form.get('senha')?.hasError('required') && form.get('senha')?.touched) {
              <mat-error>Senha obrigatória</mat-error>
            }
          </mat-form-field>

          @if (error) {
            <div class="banner banner-error">{{ error }}</div>
          }

          <button mat-flat-button color="primary" type="submit"
                  class="submit-btn" [disabled]="loading">
            @if (loading) { <mat-spinner diameter="18" /> }
            @else { <mat-icon>login</mat-icon> }
            Entrar
          </button>
        </form>

        <p class="hint">Credenciais padrão: <code>admin&#64;admin.com.br</code> / <code>admin123</code></p>
      </div>
    </div>
  `,
  styles: [`
    .login-page {
      min-height: 100vh;
      background: var(--bg);
      display: flex; align-items: center; justify-content: center;
      padding: 24px;
    }
    .login-card {
      width: 100%; max-width: 400px;
      background: #fff;
      border: 1px solid var(--border);
      border-radius: 16px;
      padding: 40px 36px;
      box-shadow: var(--shadow-lg);
    }
    .login-brand { text-align: center; margin-bottom: 32px; }
    .brand-mark {
      width: 52px; height: 52px; border-radius: 14px;
      background: var(--accent); color: #fff;
      display: flex; align-items: center; justify-content: center;
      font-size: .95rem; font-weight: 800; margin: 0 auto 14px;
    }
    .brand-title { font-size: 1.4rem; font-weight: 700; margin: 0 0 5px; color: var(--text); }
    .brand-sub   { color: var(--muted); margin: 0; font-size: .85rem; }

    .banner {
      display: flex; align-items: center; gap: 8px;
      padding: 10px 14px; border-radius: 8px;
      font-size: .84rem; margin-bottom: 16px;
    }
    .banner mat-icon { font-size: 18px !important; width: 18px !important; height: 18px !important; }
    .banner-warn  { background: #fffbeb; border: 1px solid #fde68a; color: #92400e; }
    .banner-error { background: #fef2f2; border: 1px solid #fca5a5; color: #991b1b; }

    form { display: flex; flex-direction: column; gap: 2px; }
    .fw { width: 100%; }
    .field-icon { color: var(--muted) !important; font-size: 19px !important; margin-right: 6px; }

    .submit-btn {
      height: 46px; font-size: .93rem; font-weight: 600;
      margin-top: 10px; display: flex; align-items: center; gap: 8px;
    }
    .hint { text-align: center; color: var(--muted); font-size: .75rem; margin: 18px 0 0; }
    code {
      color: var(--accent); background: var(--accent-dim);
      padding: 2px 6px; border-radius: 4px; font-size: .8rem;
    }
  `]
})
export class LoginComponent {
  private fb     = inject(FormBuilder);
  private auth   = inject(AuthService);
  private router = inject(Router);
  private route  = inject(ActivatedRoute);

  form = this.fb.group({
    email: [DEFAULT_EMAIL, [Validators.required, Validators.email]],
    senha: ['admin123', Validators.required]
  });
  loading = false; showPass = false; error = '';
  expired = this.route.snapshot.queryParamMap.has('expired');

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true; this.error = '';
    this.auth.login(this.form.value as any).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: e => { this.loading = false; this.error = e.error?.detail ?? 'E-mail ou senha inválidos'; }
    });
  }
}
