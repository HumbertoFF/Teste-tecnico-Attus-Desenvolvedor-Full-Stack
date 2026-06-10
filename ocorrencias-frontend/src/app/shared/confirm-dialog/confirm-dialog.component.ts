import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmLabel?: string;
  isDanger?: boolean;
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <div mat-dialog-title class="dlg-title">
      <mat-icon [class.danger-icon]="data.isDanger">{{ data.isDanger ? 'warning' : 'help_outline' }}</mat-icon>
      {{ data.title }}
    </div>
    <mat-dialog-content>
      <p class="dlg-msg">{{ data.message }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="ref.close(false)">Cancelar</button>
      <button mat-flat-button [color]="data.isDanger ? 'warn' : 'primary'" (click)="ref.close(true)">
        {{ data.confirmLabel ?? 'Confirmar' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .dlg-title { display: flex; align-items: center; gap: 10px; font-size: 1.05rem; font-weight: 600; }
    .danger-icon { color: var(--danger) !important; }
    .dlg-msg { color: var(--muted); margin: 0; line-height: 1.6; }
  `]
})
export class ConfirmDialogComponent {
  constructor(public ref: MatDialogRef<ConfirmDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData) {}
}
