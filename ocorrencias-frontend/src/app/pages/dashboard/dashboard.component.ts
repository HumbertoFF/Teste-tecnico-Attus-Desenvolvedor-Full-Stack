import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { OcorrenciasService } from '../../core/services/ocorrencias.service';
import { ClientesService } from '../../core/services/clientes.service';
import { OcorrenciaResponse } from '../../core/models';
import { StatusBadgeComponent } from '../../shared/status-badge/status-badge.component';

interface Stat { label: string; icon: string; color: string; bg: string; value: number; }

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
    <div class="stats-row">
      @for (s of stats; track s.label) {
        <div class="stat-card">
          <div class="stat-icon" [style.background]="s.bg">
            <mat-icon [style.color]="s.color">{{ s.icon }}</mat-icon>
          </div>
          <div class="stat-body">
            <div class="stat-num">{{ loading ? '—' : s.value }}</div>
            <div class="stat-label">{{ s.label }}</div>
          </div>
        </div>
      }
    </div>

    <!-- Recentes -->
    <div class="section-card">
      <div class="list-header">
        <span class="section-title" style="margin:0">Últimas Ocorrências</span>
        <div style="display:flex;gap:8px">
          <a mat-stroked-button routerLink="/ocorrencias/nova" class="hdr-btn">
            <mat-icon>add</mat-icon> Nova
          </a>
          <a mat-stroked-button routerLink="/ocorrencias" class="hdr-btn">
            Ver todas <mat-icon>arrow_forward</mat-icon>
          </a>
        </div>
      </div>

      @if (loading) {
        <div class="skeletons">
          @for (i of [1,2,3,4]; track i) {
            <div class="sk-row">
              <div class="sk sk-id"></div>
              <div class="sk sk-name"></div>
              <div class="sk sk-city"></div>
              <div class="sk sk-badge"></div>
            </div>
          }
        </div>
      } @else if (!recent.length) {
        <div class="empty-state">
          <div class="empty-icon">📋</div>
          <p>Nenhuma ocorrência cadastrada.</p>
          <a mat-flat-button color="primary" routerLink="/ocorrencias/nova" style="margin-top:14px">
            Cadastrar a primeira
          </a>
        </div>
      } @else {
        <div class="occ-list">
          @for (o of recent; track o.codOcorrencia) {
            <a [routerLink]="['/ocorrencias', o.codOcorrencia]" class="occ-row">
              <span class="occ-id">#{{ o.codOcorrencia }}</span>
              <div class="occ-info">
                <span class="occ-name">{{ o.cliente?.nmeCliente }}</span>
                <span class="occ-loc">{{ o.endereco?.nmeCidade }}, {{ o.endereco?.nmeEstado }}</span>
              </div>
              <span class="occ-date">{{ o.dtaOcorrencia | date:'dd/MM/yyyy' }}</span>
              <app-status-badge [status]="o.staOcorrencia" />
              <mat-icon class="occ-arrow">chevron_right</mat-icon>
            </a>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .stats-row {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 14px;
      margin-bottom: 20px;
    }
    .stat-card {
      background: #fff;
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 18px 20px;
      display: flex; align-items: center; gap: 16px;
      box-shadow: var(--shadow-sm);
      transition: box-shadow .15s, transform .15s;
    }
    .stat-card:hover { box-shadow: var(--shadow); transform: translateY(-1px); }
    .stat-icon {
      width: 44px; height: 44px; border-radius: 11px;
      display: flex; align-items: center; justify-content: center; flex-shrink: 0;
    }
    .stat-icon mat-icon { font-size: 22px !important; width: 22px !important; height: 22px !important; }
    .stat-num   { font-size: 1.8rem; font-weight: 700; color: var(--text); line-height: 1; }
    .stat-label { font-size: .78rem; color: var(--muted); margin-top: 5px; }

    .list-header {
      display: flex; align-items: center; justify-content: space-between;
      margin-bottom: 14px;
    }
    .hdr-btn {
      font-size: .78rem !important; height: 30px !important;
      padding: 0 10px !important;
      display: inline-flex !important; align-items: center !important; gap: 4px !important;
    }
    .hdr-btn mat-icon { font-size: 15px !important; width: 15px !important; height: 15px !important; }

    /* Skeleton */
    .skeletons { display: flex; flex-direction: column; gap: 6px; }
    .sk-row {
      display: flex; align-items: center; gap: 14px;
      padding: 12px 8px; border-radius: 8px;
      border-bottom: 1px solid var(--border);
    }
    .sk { background: var(--surface2); border-radius: 4px; height: 13px; animation: pulse 1.4s ease infinite; }
    .sk-id    { width: 26px; flex-shrink: 0; }
    .sk-name  { flex: 1; }
    .sk-city  { width: 90px; }
    .sk-badge { width: 60px; height: 20px; border-radius: 20px; }
    @keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.45} }

    /* List */
    .occ-list { display: flex; flex-direction: column; }
    .occ-row {
      display: flex; align-items: center; gap: 14px;
      padding: 11px 8px; border-radius: 8px;
      text-decoration: none; color: inherit;
      border-bottom: 1px solid var(--border);
      transition: background .12s;
    }
    .occ-row:last-child { border-bottom: none; }
    .occ-row:hover { background: var(--surface2); }
    .occ-id    { font-size: .74rem; font-weight: 700; color: var(--accent); min-width: 28px; flex-shrink: 0; }
    .occ-info  { flex: 1; min-width: 0; }
    .occ-name  { display: block; font-size: .875rem; font-weight: 500; color: var(--text);
                 white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .occ-loc   { display: block; font-size: .74rem; color: var(--muted); margin-top: 1px; }
    .occ-date  { font-size: .78rem; color: var(--muted); white-space: nowrap; }
    .occ-arrow { font-size: 17px !important; width: 17px !important; height: 17px !important; color: var(--muted) !important; }
  `]
})
export class DashboardComponent implements OnInit {
  private occSvc = inject(OcorrenciasService);
  private cliSvc = inject(ClientesService);

  loading = true;
  recent: OcorrenciaResponse[] = [];
  stats: Stat[] = [
    { label: 'Total de Ocorrências', icon: 'assignment',        color: '#6366f1', bg: '#eef2ff', value: 0 },
    { label: 'Ocorrências Ativas',   icon: 'pending_actions',   color: '#10b981', bg: '#ecfdf5', value: 0 },
    { label: 'Finalizadas',          icon: 'check_circle',      color: '#f59e0b', bg: '#fffbeb', value: 0 },
    { label: 'Clientes',             icon: 'people_outline',    color: '#8b5cf6', bg: '#f5f3ff', value: 0 },
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
