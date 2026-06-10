import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { TopbarComponent } from '../topbar/topbar.component';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, TopbarComponent],
  template: `
    <div class="shell">
      <app-sidebar />
      <div class="shell-main">
        <app-topbar />
        <div class="shell-content">
          <router-outlet />
        </div>
      </div>
    </div>
  `,
  styles: [`
    .shell { display: flex; height: 100vh; overflow: hidden; background: var(--bg); }
    .shell-main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
    .shell-content { flex: 1; overflow-y: auto; padding: 32px 36px; }
    @media (max-width: 768px) { .shell-content { padding: 20px 16px; } }
  `]
})
export class ShellComponent {}
