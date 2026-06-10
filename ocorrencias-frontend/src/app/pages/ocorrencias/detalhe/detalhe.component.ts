import { Component, OnInit, inject, signal, Input } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { DatePipe } from '@angular/common';
import { OcorrenciasService } from '../../../core/services/ocorrencias.service';
import { OcorrenciaResponse } from '../../../core/models';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';
import { UploadZoneComponent } from '../../../shared/upload-zone/upload-zone.component';

@Component({
  selector: 'app-detalhe',
  standalone: true,
  imports: [
    RouterLink, DatePipe,
    MatButtonModule, MatIconModule, MatProgressSpinnerModule,
    MatDividerModule, MatTooltipModule, MatChipsModule,
    StatusBadgeComponent, UploadZoneComponent
  ],
  template: `
    @if (loading()) {
      <div class="full-spinner"><mat-spinner diameter="42" /></div>
    } @else if (!occ()) {
      <div class="empty-state" style="padding-top:80px">
        <div class="empty-icon">❌</div>
        <p>Ocorrência não encontrada ou você não tem permissão.</p>
        <a mat-flat-button color="primary" routerLink="/ocorrencias" style="margin-top:20px">
          <mat-icon>arrow_back</mat-icon> Voltar à lista
        </a>
      </div>
    } @else {
      <!-- Header -->
      <div class="page-header">
        <div class="detail-header">
          <div class="detail-title-block">
            <a mat-icon-button routerLink="/ocorrencias" matTooltip="Voltar" class="back-btn">
              <mat-icon>arrow_back</mat-icon>
            </a>
            <div>
              <h1>Ocorrência #{{ occ()!.codOcorrencia }}</h1>
              <p>Registrada em {{ occ()!.dtaOcorrencia | date:'dd/MM/yyyy' }}</p>
            </div>
          </div>
          <div class="detail-actions">
            <app-status-badge [status]="occ()!.staOcorrencia" />
            @if (occ()!.staOcorrencia === 'ATIVA') {
              <button mat-stroked-button color="accent" (click)="finalizar()" class="action-btn">
                <mat-icon>check_circle_outline</mat-icon> Finalizar
              </button>
              <button mat-stroked-button color="warn" (click)="deletar()" class="action-btn">
                <mat-icon>delete_outline</mat-icon> Remover
              </button>
            }
          </div>
        </div>
      </div>

      <!-- Info grid -->
      <div class="info-grid">
        <!-- Cliente -->
        <div class="section-card">
          <div class="section-title-row">
            <mat-icon class="stitle-icon">person</mat-icon>
            <span class="section-title" style="margin:0">Dados do Cliente</span>
          </div>
          <div class="info-rows">
            <div class="info-row"><span class="ir-label">Nome</span><span class="ir-value">{{ occ()!.cliente?.nmeCliente }}</span></div>
            <div class="info-row"><span class="ir-label">CPF</span><span class="ir-value mono">{{ occ()!.cliente?.nroCpf }}</span></div>
            <div class="info-row"><span class="ir-label">Nascimento</span><span class="ir-value">{{ occ()!.cliente?.dtaNascimento | date:'dd/MM/yyyy' }}</span></div>
            <div class="info-row"><span class="ir-label">Cadastro</span><span class="ir-value">{{ occ()!.cliente?.dtaCriacao | date:'dd/MM/yyyy HH:mm' }}</span></div>
          </div>
        </div>

        <!-- Endereço -->
        <div class="section-card">
          <div class="section-title-row">
            <mat-icon class="stitle-icon">location_on</mat-icon>
            <span class="section-title" style="margin:0">Endereço da Ocorrência</span>
          </div>
          <div class="info-rows">
            <div class="info-row"><span class="ir-label">Logradouro</span><span class="ir-value">{{ occ()!.endereco?.nmeLogradouro }}</span></div>
            <div class="info-row"><span class="ir-label">Bairro</span><span class="ir-value">{{ occ()!.endereco?.nmeBairro }}</span></div>
            <div class="info-row"><span class="ir-label">CEP</span><span class="ir-value mono">{{ occ()!.endereco?.nroCep }}</span></div>
            <div class="info-row"><span class="ir-label">Cidade / Estado</span><span class="ir-value">{{ occ()!.endereco?.nmeCidade }} / {{ occ()!.endereco?.nmeEstado }}</span></div>
          </div>
        </div>
      </div>

      <!-- Arquivos -->
      <div class="section-card">
        <div class="fotos-header">
          <div class="section-title-row" style="margin-bottom:0">
            <mat-icon class="stitle-icon">attach_file</mat-icon>
            <span class="section-title" style="margin:0">
              Arquivos Anexos
              <span class="count-chip">{{ occ()!.fotos?.length ?? 0 }}</span>
            </span>
          </div>
          @if (occ()!.staOcorrencia === 'ATIVA') {
            <button mat-stroked-button (click)="showUpload = !showUpload" class="action-btn">
              <mat-icon>{{ showUpload ? 'close' : 'cloud_upload' }}</mat-icon>
              {{ showUpload ? 'Cancelar' : 'Adicionar arquivos' }}
            </button>
          }
        </div>

        @if (showUpload) {
          <div class="upload-area">
            <app-upload-zone (filesChange)="pendingFiles = $event" />
            <div class="upload-submit">
              <button mat-flat-button color="primary"
                      [disabled]="!pendingFiles.length || uploading()"
                      (click)="uploadFiles()">
                @if (uploading()) { <mat-spinner diameter="16" style="margin-right:6px" /> }
                @else { <mat-icon>send</mat-icon> }
                Enviar {{ pendingFiles.length }} arquivo(s)
              </button>
            </div>
          </div>
        }

        @if (!occ()!.fotos?.length && !showUpload) {
          <div class="empty-state" style="padding: 28px 16px">
            <div class="empty-icon" style="font-size:2rem">📎</div>
            <p>Nenhum arquivo anexado a esta ocorrência.</p>
          </div>
        } @else if (occ()!.fotos?.length) {
          <div class="fotos-grid">
            @for (foto of occ()!.fotos; track foto.codFotoOcorrencia) {
              <a [href]="foto.urlAcesso" target="_blank" class="foto-card" [title]="'Abrir arquivo #' + foto.codFotoOcorrencia">
                <div class="foto-thumb">
                  <img [src]="foto.urlAcesso" alt="Arquivo" loading="lazy" (error)="onImgError($event)" />
                  <div class="foto-overlay">
                    <mat-icon>open_in_new</mat-icon>
                  </div>
                </div>
                <div class="foto-foot">
                  <span class="foto-id">#{{ foto.codFotoOcorrencia }}</span>
                  <span class="foto-date">{{ foto.dtaCriacao | date:'dd/MM HH:mm' }}</span>
                </div>
              </a>
            }
          </div>
        }
      </div>
    }
  `,
  styles: [`
    .full-spinner { display: flex; justify-content: center; align-items: center; min-height: 60vh; }

    /* Header */
    .detail-header {
      display: flex; align-items: flex-start;
      justify-content: space-between; gap: 16px; flex-wrap: wrap;
    }
    .detail-title-block { display: flex; align-items: center; gap: 10px; }
    .back-btn { color: var(--muted) !important; }
    .detail-actions { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
    .action-btn { font-size: .82rem !important; height: 34px !important; padding: 0 14px !important; }

    /* Info grid */
    .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px; }
    @media(max-width:720px) { .info-grid { grid-template-columns: 1fr; } }

    /* Section title row */
    .section-title-row {
      display: flex; align-items: center; gap: 8px; margin-bottom: 14px;
    }
    .stitle-icon {
      font-size: 18px !important; width: 18px !important; height: 18px !important;
      color: var(--accent);
    }

    /* Info rows */
    .info-rows { display: flex; flex-direction: column; }
    .info-row {
      display: flex; justify-content: space-between; align-items: baseline;
      gap: 12px; padding: 8px 0;
      border-bottom: 1px solid var(--border);
    }
    .info-row:last-child { border-bottom: none; }
    .ir-label { font-size: .8rem; color: var(--muted); white-space: nowrap; flex-shrink: 0; }
    .ir-value { font-size: .88rem; font-weight: 500; text-align: right; }
    .mono     { font-family: 'JetBrains Mono', monospace; font-size: .84rem; }

    /* Arquivos header */
    .fotos-header {
      display: flex; align-items: center;
      justify-content: space-between; margin-bottom: 16px; gap: 12px; flex-wrap: wrap;
    }
    .count-chip {
      background: var(--accent-dim); color: var(--accent);
      font-size: .72rem; font-weight: 700; padding: 1px 8px;
      border-radius: 20px; margin-left: 8px; vertical-align: middle;
    }

    /* Upload */
    .upload-area {
      background: var(--surface2);
      border: 1px solid var(--border);
      border-radius: 8px; padding: 16px; margin-bottom: 16px;
    }
    .upload-submit { margin-top: 12px; display: flex; gap: 8px; }

    /* Fotos grid */
    .fotos-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
      gap: 12px;
    }
    .foto-card {
      border: 1px solid var(--border); border-radius: 10px;
      overflow: hidden; text-decoration: none; color: inherit;
      transition: border-color .15s, transform .15s, box-shadow .15s;
      display: block;
    }
    .foto-card:hover {
      border-color: var(--accent); transform: translateY(-2px);
      box-shadow: 0 8px 20px rgba(0,0,0,.35);
    }
    .foto-thumb {
      height: 90px; background: var(--surface2);
      overflow: hidden; position: relative;
      display: flex; align-items: center; justify-content: center;
    }
    .foto-thumb img { width: 100%; height: 100%; object-fit: cover; }
    .foto-overlay {
      position: absolute; inset: 0;
      background: rgba(0,0,0,.5);
      display: flex; align-items: center; justify-content: center;
      opacity: 0; transition: opacity .15s;
    }
    .foto-overlay mat-icon { color: #fff; font-size: 22px !important; }
    .foto-card:hover .foto-overlay { opacity: 1; }
    .foto-foot {
      display: flex; align-items: center; gap: 6px;
      padding: 7px 9px; border-top: 1px solid var(--border);
    }
    .foto-id   { font-size: .73rem; color: var(--accent); font-weight: 700; }
    .foto-date { font-size: .72rem; color: var(--muted); flex: 1; text-align: right; }
  `]
})
export class DetalheComponent implements OnInit {
  @Input() id!: string;

  private svc    = inject(OcorrenciasService);
  private snack  = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private router = inject(Router);

  occ       = signal<OcorrenciaResponse | null>(null);
  loading   = signal(true);
  uploading = signal(false);
  showUpload = false;
  pendingFiles: File[] = [];

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    this.svc.buscarPorId(+this.id).subscribe({
      next: d => { this.occ.set(d); this.loading.set(false); },
      error: () => { this.occ.set(null); this.loading.set(false); }
    });
  }

  finalizar() {
    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Finalizar Ocorrência',
        message: `Deseja finalizar a ocorrência #${this.id}? Ela se tornará imutável e não poderá mais ser editada ou removida.`,
        confirmLabel: 'Finalizar'
      }
    }).afterClosed().subscribe(ok => {
      if (!ok) return;
      this.svc.finalizar(+this.id).subscribe({
        next: () => {
          this.snack.open('Ocorrência finalizada com sucesso!', '', { duration: 3500, panelClass: 'success-snack' });
          this.load();
        },
        error: e => this.snack.open(e.error?.detail ?? 'Erro ao finalizar', '', { duration: 4000, panelClass: 'error-snack' })
      });
    });
  }

  deletar() {
    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Remover Ocorrência',
        message: `Confirma a remoção permanente da ocorrência #${this.id}? Esta ação não pode ser desfeita.`,
        confirmLabel: 'Remover',
        isDanger: true
      }
    }).afterClosed().subscribe(ok => {
      if (!ok) return;
      this.svc.deletar(+this.id).subscribe({
        next: () => {
          this.snack.open('Ocorrência removida.', '', { duration: 3000, panelClass: 'success-snack' });
          this.router.navigate(['/ocorrencias']);
        },
        error: e => this.snack.open(e.error?.detail ?? 'Erro ao remover', '', { duration: 4000, panelClass: 'error-snack' })
      });
    });
  }

  uploadFiles() {
    this.uploading.set(true);
    this.svc.adicionarArquivos(+this.id, this.pendingFiles).subscribe({
      next: () => {
        this.uploading.set(false);
        this.showUpload = false;
        this.pendingFiles = [];
        this.snack.open('Arquivos enviados com sucesso!', '', { duration: 3000, panelClass: 'success-snack' });
        this.load();
      },
      error: e => {
        this.uploading.set(false);
        this.snack.open(e.error?.detail ?? 'Erro ao enviar arquivos', '', { duration: 4000, panelClass: 'error-snack' });
      }
    });
  }

  onImgError(event: Event) {
    const img = event.target as HTMLImageElement;
    img.style.display = 'none';
    const parent = img.closest('.foto-thumb');
    if (parent) parent.innerHTML = '<mat-icon style="font-size:2rem;color:var(--muted);display:flex;align-items:center;justify-content:center;height:100%">description</mat-icon>';
  }
}
