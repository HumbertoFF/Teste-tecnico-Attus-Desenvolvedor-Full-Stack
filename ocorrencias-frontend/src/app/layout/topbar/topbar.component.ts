import { Component, inject, signal, HostListener, ElementRef } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [MatIconModule, MatDividerModule, RouterLink],
  template: `
    <header class="topbar">
      <div class="spacer"></div>

      <div class="user-wrap">
        <button class="user-btn" (click)="toggle()" type="button" [class.open]="open()">
          <span class="avatar">{{ initial }}</span>
          <span class="user-email">{{ auth.email() }}</span>
          <mat-icon class="chevron" [class.rotated]="open()">expand_more</mat-icon>
        </button>

        @if (open()) {
          <div class="dropdown">
            <div class="dp-profile">
              <span class="dp-avatar">{{ initial }}</span>
              <div>
                <div class="dp-name">{{ auth.email() }}</div>
                <div class="dp-role">Administrador</div>
              </div>
            </div>
            <mat-divider />
            <a class="dp-item" routerLink="/dashboard" (click)="close()">
              <mat-icon>dashboard</mat-icon> Dashboard
            </a>
            <a class="dp-item" routerLink="/clientes" (click)="close()">
              <mat-icon>people</mat-icon> Clientes
            </a>
            <mat-divider />
            <button class="dp-item dp-logout" (click)="logout()">
              <mat-icon>logout</mat-icon> Sair
            </button>
          </div>
        }
      </div>
    </header>
  `,
  styles: [`
    .topbar {
      height: 56px;
      background: #fff;
      border-bottom: 1px solid var(--border);
      display: flex;
      align-items: center;
      padding: 0 20px;
      flex-shrink: 0;
    }
    .spacer { flex: 1; }

    /* Wrapper com position:relative é a âncora do dropdown */
    .user-wrap {
      position: relative;
      display: flex;
      align-items: center;
    }

    .user-btn {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 5px 10px;
      border: 1px solid transparent;
      border-radius: 8px;
      background: transparent;
      cursor: pointer;
      transition: background .12s, border-color .12s;
      font-family: inherit;
    }
    .user-btn:hover, .user-btn.open {
      background: var(--surface2);
      border-color: var(--border);
    }

    .avatar {
      width: 28px; height: 28px; border-radius: 50%;
      background: var(--accent); color: #fff;
      display: flex; align-items: center; justify-content: center;
      font-weight: 700; font-size: .8rem; flex-shrink: 0;
    }
    .user-email {
      font-size: .82rem; color: var(--text2); font-weight: 500;
      max-width: 180px; overflow: hidden;
      text-overflow: ellipsis; white-space: nowrap;
    }
    .chevron {
      font-size: 18px !important; width: 18px !important;
      height: 18px !important; color: var(--muted);
      transition: transform .15s;
    }
    .chevron.rotated { transform: rotate(180deg); }

    /* Dropdown — posicionado relativo ao .user-wrap */
    .dropdown {
      position: absolute;
      top: calc(100% + 6px); /* logo abaixo do botão */
      right: 0;              /* alinhado à direita */
      min-width: 220px;
      background: #fff;
      border: 1px solid var(--border);
      border-radius: 10px;
      box-shadow: 0 8px 24px rgba(0,0,0,.10);
      z-index: 1000;
      overflow: hidden;
      animation: fadeDown .12s ease;
    }
    @keyframes fadeDown {
      from { opacity: 0; transform: translateY(-6px); }
      to   { opacity: 1; transform: translateY(0); }
    }

    .dp-profile {
      display: flex; align-items: center; gap: 10px;
      padding: 14px 16px 12px;
    }
    .dp-avatar {
      width: 32px; height: 32px; border-radius: 50%;
      background: var(--accent); color: #fff;
      display: flex; align-items: center; justify-content: center;
      font-weight: 700; font-size: .84rem; flex-shrink: 0;
    }
    .dp-name { font-size: .85rem; font-weight: 600; color: var(--text); }
    .dp-role { font-size: .72rem; color: var(--muted); margin-top: 1px; }

    .dp-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px 16px;
      font-size: .875rem;
      color: var(--text2);
      text-decoration: none;
      background: transparent;
      border: none;
      width: 100%;
      text-align: left;
      cursor: pointer;
      font-family: inherit;
      transition: background .1s;
    }
    .dp-item:hover { background: var(--surface2); }
    .dp-item mat-icon {
      font-size: 18px !important; width: 18px !important;
      height: 18px !important; color: var(--muted);
    }

    .dp-logout { color: var(--danger) !important; }
    .dp-logout mat-icon { color: var(--danger) !important; }
  `]
})
export class TopbarComponent {
  auth    = inject(AuthService);
  router  = inject(Router);
  elRef   = inject(ElementRef);
  open    = signal(false);

  get initial() { return (this.auth.email() ?? 'A').charAt(0).toUpperCase(); }

  toggle() { this.open.update(v => !v); }
  close()  { this.open.set(false); }

  logout() { this.close(); this.auth.logout(); }

  // Fecha ao clicar fora do componente
  @HostListener('document:click', ['$event'])
  onDocClick(e: MouseEvent) {
    if (!this.elRef.nativeElement.contains(e.target)) {
      this.close();
    }
  }
}
