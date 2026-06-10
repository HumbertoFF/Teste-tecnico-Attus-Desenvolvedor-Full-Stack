import { Component, OnInit, inject, signal, Pipe, PipeTransform } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DatePipe } from '@angular/common';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { OcorrenciasService } from '../../../core/services/ocorrencias.service';
import { OcorrenciaResponse, PageResponse } from '../../../core/models';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';

@Pipe({ name: 'cpfPipe', standalone: true })
export class CpfPipe implements PipeTransform {
  transform(v: string): string {
    if (!v) return '';
    const c = v.replace(/\D/g, '');
    return c.replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, '$1.$2.$3-$4');
  }
}

@Component({
  selector: 'app-ocorrencias-lista',
  standalone: true,
  imports: [
    RouterLink, ReactiveFormsModule, DatePipe, CpfPipe,
    MatTableModule, MatPaginatorModule, MatSortModule,
    MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatProgressSpinnerModule,
    MatTooltipModule, StatusBadgeComponent
  ],
  template: `
    <div class="page-header">
      <h1>Ocorrências</h1>
      <p>Listagem com filtros, paginação e gerenciamento de status</p>
    </div>

    <div class="section-card">
      <form [formGroup]="filterForm" class="filters-row">
        <mat-form-field appearance="fill" class="filter-field">
          <mat-label>Nome do cliente</mat-label>
          <mat-icon matPrefix>person_search</mat-icon>
          <input matInput formControlName="nmeCliente" />
        </mat-form-field>

        <mat-form-field appearance="fill" class="filter-field">
          <mat-label>CPF</mat-label>
          <mat-icon matPrefix>badge</mat-icon>
          <input matInput formControlName="nroCpf" />
        </mat-form-field>

        <mat-form-field appearance="fill" class="filter-field">
          <mat-label>Cidade</mat-label>
          <mat-icon matPrefix>location_city</mat-icon>
          <input matInput formControlName="nmeCidade" />
        </mat-form-field>

        <mat-form-field appearance="fill" class="filter-field">
          <mat-label>Data</mat-label>
          <input matInput type="date" formControlName="dtaOcorrencia" />
        </mat-form-field>

        <div class="filter-select-wrap">
          <label class="filter-select-label">Ordenar</label>
          <select class="filter-select" formControlName="sort">
            <option value="dtaOcorrencia,desc">Data ↓</option>
            <option value="dtaOcorrencia,asc">Data ↑</option>
            <option value="endereco.nmeCidade,asc">Cidade A→Z</option>
            <option value="endereco.nmeCidade,desc">Cidade Z→A</option>
          </select>
        </div>

        <div class="filter-actions">
          <button mat-stroked-button type="button" (click)="clearFilters()">
            <mat-icon>clear</mat-icon> Limpar
          </button>
          <button mat-flat-button color="primary" routerLink="/ocorrencias/nova">
            <mat-icon>add</mat-icon> Nova Ocorrência
          </button>
        </div>
      </form>
    </div>

    <div class="section-card">
      @if (loading()) {
        <div class="center-spinner"><mat-spinner diameter="36" /></div>
      } @else if (data.content.length === 0) {
        <div class="empty-state">
          <div class="empty-icon">🔍</div>
          <p>Nenhuma ocorrência encontrada com os filtros aplicados.</p>
        </div>
      } @else {
        <div class="table-container">
          <table mat-table [dataSource]="data.content" class="full-table">
            <ng-container matColumnDef="id">
              <th mat-header-cell *matHeaderCellDef class="col-id">#</th>
              <td mat-cell *matCellDef="let o" class="col-id">
                <span class="id-chip">{{ o.codOcorrencia }}</span>
              </td>
            </ng-container>
            <ng-container matColumnDef="cliente">
              <th mat-header-cell *matHeaderCellDef>Cliente</th>
              <td mat-cell *matCellDef="let o">
                <div class="cell-primary">{{ o.cliente?.nmeCliente }}</div>
                <div class="cell-secondary">{{ o.cliente?.nroCpf | cpfPipe }}</div>
              </td>
            </ng-container>
            <ng-container matColumnDef="cidade">
              <th mat-header-cell *matHeaderCellDef>Cidade / Estado</th>
              <td mat-cell *matCellDef="let o">{{ o.endereco?.nmeCidade }} / {{ o.endereco?.nmeEstado }}</td>
            </ng-container>
            <ng-container matColumnDef="data">
              <th mat-header-cell *matHeaderCellDef>Data</th>
              <td mat-cell *matCellDef="let o">{{ o.dtaOcorrencia | date:'dd/MM/yyyy' }}</td>
            </ng-container>
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let o"><app-status-badge [status]="o.staOcorrencia" /></td>
            </ng-container>
            <ng-container matColumnDef="fotos">
              <th mat-header-cell *matHeaderCellDef>Anexos</th>
              <td mat-cell *matCellDef="let o">
                <span class="fotos-count">{{ o.fotos?.length ?? 0 }}</span>
              </td>
            </ng-container>
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef class="col-actions"></th>
              <td mat-cell *matCellDef="let o" class="col-actions">
                <button mat-icon-button [routerLink]="['/ocorrencias', o.codOcorrencia]" matTooltip="Ver detalhes">
                  <mat-icon>visibility</mat-icon>
                </button>
                @if (o.staOcorrencia === 'ATIVA') {
                  <button mat-icon-button color="accent" (click)="finalizar(o,$event)" matTooltip="Finalizar">
                    <mat-icon>check_circle</mat-icon>
                  </button>
                  <button mat-icon-button color="warn" (click)="deletar(o,$event)" matTooltip="Remover">
                    <mat-icon>delete</mat-icon>
                  </button>
                }
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="cols"></tr>
            <tr mat-row *matRowDef="let row; columns: cols;"
                [routerLink]="['/ocorrencias', row.codOcorrencia]"
                style="cursor:pointer"></tr>
          </table>
        </div>
        <mat-paginator [length]="data.totalElements" [pageSize]="pageSize"
          [pageSizeOptions]="[10,25,50]" (page)="onPage($event)" showFirstLastButtons />
      }
    </div>
  `,
  styles: [`
    /* Linha de filtros — align-items:flex-end alinha pela base de todos os elementos */
    .filters-row{display:flex;flex-wrap:wrap;gap:10px;align-items:flex-end}
    .filter-field{min-width:150px;flex:1;max-width:210px}

    /* Select nativo com mesma altura e margem do mat-form-field fill */
    .filter-select-wrap{display:flex;flex-direction:column;min-width:150px;flex:1;max-width:200px;margin-bottom:22px}
    .filter-select-label{font-size:.75rem;color:rgba(0,0,0,.6);padding:0 0 2px 12px;line-height:1}
    .filter-select{
      height:40px;padding:0 32px 0 12px;
      border:none;border-bottom:1px solid rgba(0,0,0,.42);border-radius:4px 4px 0 0;
      background:var(--surface2) url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24'%3E%3Cpath fill='%239ca3af' d='M7 10l5 5 5-5z'/%3E%3C/svg%3E") no-repeat right 8px center;
      appearance:none;-webkit-appearance:none;
      color:var(--text);font-family:'Inter',system-ui,sans-serif;font-size:.875rem;
      cursor:pointer;outline:none;transition:border-color .15s,background-color .15s;
    }
    .filter-select:hover{background-color:#eaebf5}
    .filter-select:focus{border-bottom:2px solid var(--accent);background-color:#eaebf5}
    .filter-select option{background:#fff;color:var(--text)}

    /* Botões com mesmo margin-bottom do mat-form-field */
    .filter-actions{display:flex;gap:8px;align-items:center;margin-bottom:22px;flex-shrink:0}

    .full-table{width:100%}
    .col-id{width:60px}
    .id-chip{background:var(--accent-dim);color:var(--accent);padding:2px 8px;border-radius:20px;font-size:.78rem;font-weight:700}
    .cell-primary{font-weight:500}
    .cell-secondary{font-size:.78rem;color:var(--muted)}
    .fotos-count{background:var(--surface2);color:var(--muted);padding:2px 8px;border-radius:20px;font-size:.78rem}
    .center-spinner{display:flex;justify-content:center;padding:40px}
  `]
})
export class ListaComponent implements OnInit {
  private svc    = inject(OcorrenciasService);
  private fb     = inject(FormBuilder);
  private snack  = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  cols = ['id','cliente','cidade','data','status','fotos','actions'];
  loading = signal(false);
  data: PageResponse<OcorrenciaResponse> = {content:[],totalElements:0,totalPages:0,number:0,size:10};
  pageSize = 10;
  currentPage = 0;

  filterForm = this.fb.group({
    nmeCliente:[''], nroCpf:[''], nmeCidade:[''], dtaOcorrencia:[''],
    sort:['dtaOcorrencia,desc']
  });

  ngOnInit() {
    this.load();
    this.filterForm.valueChanges.pipe(debounceTime(400), distinctUntilChanged()).subscribe(() => {
      this.currentPage = 0; this.load();
    });
  }

  load() {
    this.loading.set(true);
    const v = this.filterForm.value;
    this.svc.listar({
      page: this.currentPage, size: this.pageSize,
      nmeCliente:    v.nmeCliente    || undefined,
      nroCpf:        v.nroCpf        || undefined,
      nmeCidade:     v.nmeCidade     || undefined,
      dtaOcorrencia: v.dtaOcorrencia || undefined,
      sort:          v.sort          || undefined,
    }).subscribe({
      next: d => { this.data = d; this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  onPage(e: PageEvent) { this.currentPage = e.pageIndex; this.pageSize = e.pageSize; this.load(); }

  clearFilters() { this.filterForm.reset({sort:'dtaOcorrencia,desc'}); }

  finalizar(o: OcorrenciaResponse, event: Event) {
    event.stopPropagation();
    this.dialog.open(ConfirmDialogComponent, {
      data:{title:'Finalizar Ocorrência',message:`Deseja finalizar a ocorrência #${o.codOcorrencia}? Esta ação é irreversível.`,confirmLabel:'Finalizar'}
    }).afterClosed().subscribe(ok => {
      if (!ok) return;
      this.svc.finalizar(o.codOcorrencia).subscribe({
        next: () => { this.snack.open('Ocorrência finalizada!','',{duration:3000,panelClass:'success-snack'}); this.load(); },
        error: e  => this.snack.open(e.error?.detail??'Erro ao finalizar','',{duration:4000,panelClass:'error-snack'})
      });
    });
  }

  deletar(o: OcorrenciaResponse, event: Event) {
    event.stopPropagation();
    this.dialog.open(ConfirmDialogComponent, {
      data:{title:'Remover Ocorrência',message:`Confirma a remoção da ocorrência #${o.codOcorrencia}?`,confirmLabel:'Remover',isDanger:true}
    }).afterClosed().subscribe(ok => {
      if (!ok) return;
      this.svc.deletar(o.codOcorrencia).subscribe({
        next: () => { this.snack.open('Ocorrência removida!','',{duration:3000,panelClass:'success-snack'}); this.load(); },
        error: e  => this.snack.open(e.error?.detail??'Erro ao remover','',{duration:4000,panelClass:'error-snack'})
      });
    });
  }
}
