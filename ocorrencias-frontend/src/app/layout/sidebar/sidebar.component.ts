import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, MatIconModule],
  template: `
    <aside class="sidebar">
      <div class="logo">
        <div class="logo-mark">OC</div>
        <div>
          <div class="logo-title">Ocorrências</div>
          <div class="logo-sub">Gestão de Registros</div>
        </div>
      </div>
      <nav class="nav">
        @for (item of items; track item.route) {
          @if (item.sep) { <div class="sep"></div> }
          <a class="nav-item" [routerLink]="item.route"
             routerLinkActive="active"
             [routerLinkActiveOptions]="{exact: item.exact ?? false}">
            <mat-icon class="nav-icon">{{ item.icon }}</mat-icon>
            <span>{{ item.label }}</span>
          </a>
        }
      </nav>
      <div class="footer">
        <span class="dot"></span>
        <span class="footer-text">Sistema online</span>
      </div>
    </aside>
  `,
  styles: [`
    .sidebar{width:236px;height:100vh;background:#fff;border-right:1px solid var(--border);display:flex;flex-direction:column;flex-shrink:0;overflow:hidden}
    .logo{padding:20px 18px;display:flex;align-items:center;gap:11px;border-bottom:1px solid var(--border);flex-shrink:0}
    .logo-mark{width:36px;height:36px;flex-shrink:0;background:var(--accent);border-radius:9px;display:flex;align-items:center;justify-content:center;font-size:.78rem;font-weight:800;color:#fff;letter-spacing:.5px}
    .logo-title{font-size:.95rem;font-weight:700;color:var(--text);line-height:1.2}
    .logo-sub{font-size:.68rem;color:var(--muted);margin-top:1px}
    .nav{flex:1;padding:12px 10px;display:flex;flex-direction:column;gap:1px;overflow-y:auto}
    .sep{height:1px;background:var(--border);margin:6px 4px;flex-shrink:0}
    .nav-item{display:flex;align-items:center;gap:10px;padding:9px 11px;border-radius:8px;color:var(--muted);text-decoration:none;font-size:.865rem;font-weight:500;width:100%;box-sizing:border-box;flex-shrink:0;transition:background-color .12s,color .12s;overflow:hidden;white-space:nowrap}
    .nav-item:hover{background-color:var(--surface2);color:var(--text2)}
    .nav-item.active{background-color:var(--accent-dim);color:var(--accent)}
    .nav-item.active .nav-icon{color:var(--accent)}
    .nav-icon{font-size:19px!important;width:19px!important;height:19px!important;flex-shrink:0;color:inherit}
    .footer{padding:14px 18px;border-top:1px solid var(--border);display:flex;align-items:center;gap:7px;flex-shrink:0}
    .dot{width:7px;height:7px;border-radius:50%;background:var(--success);flex-shrink:0}
    .footer-text{font-size:.72rem;color:var(--muted)}
  `]
})
export class SidebarComponent {
  items = [
    { label:'Dashboard',       icon:'dashboard',          route:'/dashboard',        exact:true },
    { label:'Ocorrências',     icon:'assignment',         route:'/ocorrencias',      exact:true },
    { label:'Nova Ocorrência', icon:'add_circle_outline', route:'/ocorrencias/nova', sep:true },
    { label:'Clientes',        icon:'people_outline',     route:'/clientes',         sep:true },
  ];
}
