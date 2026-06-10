import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DatePipe } from '@angular/common';
import { OcorrenciasService } from '../../core/services/ocorrencias.service';
import { ClientesService } from '../../core/services/clientes.service';
import { OcorrenciaResponse } from '../../core/models';
import { StatusBadgeComponent } from '../../shared/status-badge/status-badge.component';
import { forkJoin } from 'rxjs';

interface StatCard {
  label: string;
  icon: string;
  gradFrom: string;
  gradTo: string;
  value: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, MatButtonModule, MatIconModule, MatProgressSpinnerModule, DatePipe, StatusBadgeComponent],
  template: `
    <div class="page-header">
      <h1>Dashboard</h1>
      <p>Visão geral do sistema · dados em tempo real</p>
    </div>

    <!-- Stats -->
    <div class="stats-grid">
      @for (s of stats; track s.label) {
        <div class="stat-card">
          <div class="stat-icon" [style]="'background: linear-gradient(135deg,' + s.gradFrom + ',' + s.gradTo + ')'">
            <mat-icon>{{ s.icon }}</mat-icon>
          </div>
          <div class="stat-body">
            @if (loading) {
              <div class="stat-skeleton"></div>
            } @else {
              <div class="stat-value">{{ s.value }}</div>
            }
            <div class="stat-label">{{ s.label }}</div>
          </div>
        </div>
      }
    </div>

    <!-- Recent list -->
    <div class="section-card">
      <div class="section-header">
        <span class="section-title">Últimas Ocorrências</span>
        <div style="display:flex;gap:8px">
          <a mat-stroked-button routerLink="/ocorrencias/nova" class="btn-sm-action">
            <mat-icon>add</mat-icon> Nova
          </a>
          <a mat-stroked-button routerLink="/ocorrencias" class="btn-sm-action">
            Ver todas <mat-icon>arrow_forward</mat-icon>
          </a>
        </div>
      </div>

      @if (loading) {
        <div class="loading-rows">
          @for (i of [1,2,3,4,5]; track i) {
            <div class="skeleton-row">
              <div class="sk sk-id"></div>
              <div class="sk sk-name"></div>
              <div class="sk sk-loc"></div>
              <div class="sk sk-badge"></div>
            </div>
          }
        </div>
      } @else if (recent.length === 0) {
        <div class="empty-state">
          <div class="empty-icon">📋</div>
          <p>Nenhuma ocorrência cadastrada ainda.</p>
          <a mat-flat-button color="primary" routerLink="/ocorrencias/nova" style="margin-top:16px">
            Cadastrar primeira ocorrência
          </a>
        </div>
      } @else {
        <div class="recent-list">
          @for (o of recent; track o.codOcorrencia) {
            <a [routerLink]="['/ocorrencias', o.codOcorrencia]" class="recent-row">
              <span class="recent-num">#{{ o.codOcorrencia }}</span>
              <div class="recent-info">
                <span class="recent-name">{{ o.cliente?.nmeCliente }}</span>
                <span class="recent-sub">{{ o.endereco?.nmeCidade }}, {{ o.endereco?.nmeEstado }}</span>
              </div>
              <span class="recent-date">{{ o.dtaOcorrencia | date:'dd/MM/yyyy' }}</span>
              <app-status-badge [status]="o.staOcorrencia" />
              <mat-icon class="row-chevron">chevron_right</mat-icon>
            </a>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    /* Stats */
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
      gap: 14px;
      margin-bottom: 24px;
    }
    .stat-card {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 18px 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      transition: border-color .2s, transform .2s;
    }
    .stat-card:hover { border-color: rgba(79,124,255,.4); transform: translateY(-1px); }
    .stat-icon {
      width: 46px; height: 46px; border-radius: 12px;
      display: flex; align-items: center; justify-content: center;
      flex-shrink: 0;
    }
    .stat-icon mat-icon { color: #fff; font-size: 22px !important; width: 22px !important; height: 22px !important; }
    .stat-body   { flex: 1; min-width: 0; }
    .stat-value  { font-size: 1.9rem; font-weight: 700; line-height: 1; color: var(--text); }
    .stat-label  { color: var(--muted); font-size: .8rem; margin-top: 5px; }
    .stat-skeleton { width: 48px; height: 28px; background: var(--surface2); border-radius: 4px; margin-bottom: 2px; }

    /* Section */
    .section-header {
      display: flex; align-items: center; justify-content: space-between;
      margin-bottom: 16px;
    }
    .btn-sm-action {
      font-size: .78rem !important;
      height: 30px !important;
      padding: 0 10px !important;
      display: inline-flex !important;
      align-items: center !important;
      gap: 4px !important;
    }
    .btn-sm-action mat-icon { font-size: 16px !important; width: 16px !important; height: 16px !important; }

    /* Loading skeleton rows */
    .loading-rows { display: flex; flex-direction: column; gap: 8px; }
    .skeleton-row {
      display: flex; align-items: center; gap: 16px;
      padding: 12px 8px;
      border-radius: 8px;
      border-bottom: 1px solid var(--border);
    }
    .sk { background: var(--surface2); border-radius: 4px; height: 14px; }
    .sk-id    { width: 28px; }
    .sk-name  { width: 140px; flex: 1; }
    .sk-loc   { width: 100px; }
    .sk-badge { width: 70px; height: 22px; border-radius: 20px; }

    /* Recent rows */
    .recent-list { display: flex; flex-direction: column; }
    .recent-row {
      display: flex;
      align-items: center;
      gap: 14px;
      padding: 12px 8px;
      border-radius: 8px;
      text-decoration: none;
      color: inherit;
      border-bottom: 1px solid transparent;
      transition: background .15s;
    }
    .recent-row:hover { background: var(--surface2); }
    .recent-row:last-child { border-bottom: none; }

    .recent-num  { font-size: .75rem; font-weight: 700; color: var(--accent); min-width: 30px; }
    .recent-info { flex: 1; min-width: 0; }
    .recent-name { display: block; font-weight: 500; font-size: .9rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .recent-sub  { display: block; font-size: .75rem; color: var(--muted); margin-top: 2px; }
    .recent-date { font-size: .8rem; color: var(--muted); white-space: nowrap; }
    .row-chevron {
      color: var(--muted) !important; font-size: 18px !important;
      width: 18px !important; height: 18px !important;
    }
  `]
})
export class DashboardComponent implements OnInit {
  private occSvc = inject(OcorrenciasService);
  private cliSvc = inject(ClientesService);

  loading = true;
  recent: OcorrenciaResponse[] = [];

  stats: StatCard[] = [
    { label: 'Total de Ocorrências', icon: 'assignment',   gradFrom: '#1e3a6e', gradTo: '#2a4a8e', value: 0 },
    { label: 'Ocorrências Ativas',   icon: 'pending_actions', gradFrom: '#1a3d2a', gradTo: '#235233', value: 0 },
    { label: 'Finalizadas',          icon: 'check_circle', gradFrom: '#3a3020', gradTo: '#4a3d28', value: 0 },
    { label: 'Clientes Cadastrados', icon: 'people',       gradFrom: '#2e1e4a', gradTo: '#3c2860', value: 0 },
  ];

  ngOnInit() {
    forkJoin({
      occ: this.occSvc.listar({ size: 6, sort: 'dtaOcorrencia,desc' }),
      cli: this.cliSvc.listar(0, 1),
    }).subscribe({
      next: ({ occ, cli }) => {
        this.recent = occ.content;
        this.stats[0].value = occ.totalElements;
        this.stats[1].value = occ.content.filter(o => o.staOcorrencia === 'ATIVA').length;
        this.stats[2].value = occ.content.filter(o => o.staOcorrencia === 'FINALIZADA').length;
        this.stats[3].value = cli.totalElements;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }
}
