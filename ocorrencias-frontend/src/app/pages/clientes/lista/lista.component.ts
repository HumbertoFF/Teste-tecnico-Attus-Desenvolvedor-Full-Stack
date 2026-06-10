import { Component, OnInit, inject, signal } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DatePipe } from '@angular/common';
import { ClientesService } from '../../../core/services/clientes.service';
import { ClienteResponse, PageResponse } from '../../../core/models';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';
import { FormComponent } from '../form/form.component';

@Component({
  selector: 'app-clientes-lista',
  standalone: true,
  imports: [
    DatePipe, MatTableModule, MatPaginatorModule,
    MatButtonModule, MatIconModule, MatProgressSpinnerModule, MatTooltipModule
  ],
  template: `
    <div class="page-header">
      <h1>Clientes</h1>
      <p>Gestão de todos os clientes cadastrados</p>
    </div>

    <div style="margin-bottom:16px">
      <button mat-flat-button color="primary" (click)="abrirForm()">
        <mat-icon>person_add</mat-icon> Novo Cliente
      </button>
    </div>

    <div class="section-card">
      @if (loading()) {
        <div class="center-spinner"><mat-spinner diameter="36" /></div>
      } @else if (data.content.length === 0) {
        <div class="empty-state">
          <div class="empty-icon">👥</div>
          <p>Nenhum cliente cadastrado ainda.</p>
        </div>
      } @else {
        <div class="table-container">
          <table mat-table [dataSource]="data.content" class="full-table">
            <ng-container matColumnDef="id">
              <th mat-header-cell *matHeaderCellDef class="col-id">#</th>
              <td mat-cell *matCellDef="let c" class="col-id">{{ c.codCliente }}</td>
            </ng-container>
            <ng-container matColumnDef="nome">
              <th mat-header-cell *matHeaderCellDef>Nome</th>
              <td mat-cell *matCellDef="let c"><strong>{{ c.nmeCliente }}</strong></td>
            </ng-container>
            <ng-container matColumnDef="cpf">
              <th mat-header-cell *matHeaderCellDef>CPF</th>
              <td mat-cell *matCellDef="let c" class="font-mono">{{ c.nroCpf }}</td>
            </ng-container>
            <ng-container matColumnDef="nascimento">
              <th mat-header-cell *matHeaderCellDef>Nascimento</th>
              <td mat-cell *matCellDef="let c">{{ c.dtaNascimento | date:'dd/MM/yyyy' }}</td>
            </ng-container>
            <ng-container matColumnDef="criacao">
              <th mat-header-cell *matHeaderCellDef>Cadastro</th>
              <td mat-cell *matCellDef="let c">{{ c.dtaCriacao | date:'dd/MM/yyyy' }}</td>
            </ng-container>
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef></th>
              <td mat-cell *matCellDef="let c" class="col-actions">
                <button mat-icon-button (click)="abrirForm(c)" matTooltip="Editar">
                  <mat-icon>edit</mat-icon>
                </button>
                <button mat-icon-button color="warn" (click)="deletar(c)" matTooltip="Remover">
                  <mat-icon>delete</mat-icon>
                </button>
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="cols"></tr>
            <tr mat-row *matRowDef="let row; columns: cols;"></tr>
          </table>
        </div>
        <mat-paginator
          [length]="data.totalElements"
          [pageSize]="pageSize"
          [pageSizeOptions]="[10, 25]"
          (page)="onPage($event)"
          showFirstLastButtons />
      }
    </div>
  `,
  styles: [`
    .full-table { width: 100%; }
    .col-id     { width: 60px; }
    .font-mono  { font-family: monospace; font-size: .88rem; }
    .center-spinner { display: flex; justify-content: center; padding: 40px; }
  `]
})
export class ListaComponent implements OnInit {
  private svc    = inject(ClientesService);
  private snack  = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  cols     = ['id', 'nome', 'cpf', 'nascimento', 'criacao', 'actions'];
  loading  = signal(false);
  data: PageResponse<ClienteResponse> = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 };
  pageSize = 10;
  currentPage = 0;

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    this.svc.listar(this.currentPage, this.pageSize).subscribe({
      next: d => { this.data = d; this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  onPage(e: PageEvent) { this.currentPage = e.pageIndex; this.pageSize = e.pageSize; this.load(); }

  abrirForm(cliente?: ClienteResponse) {
    this.dialog.open(FormComponent, { data: cliente, width: '480px' })
      .afterClosed().subscribe(saved => { if (saved) this.load(); });
  }

  deletar(c: ClienteResponse) {
    this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Remover Cliente', message: `Remover ${c.nmeCliente}?`, confirmLabel: 'Remover', isDanger: true }
    }).afterClosed().subscribe(ok => {
      if (!ok) return;
      this.svc.deletar(c.codCliente).subscribe({
        next: () => { this.snack.open('Cliente removido!', '', { duration: 3000, panelClass: 'success-snack' }); this.load(); },
        error: e => this.snack.open(e.error?.detail ?? 'Erro', '', { duration: 4000, panelClass: 'error-snack' })
      });
    });
  }
}
