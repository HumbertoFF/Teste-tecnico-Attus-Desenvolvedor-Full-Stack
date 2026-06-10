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
      <div class="dlg-icon" [class.danger]="data.isDanger">
        <mat-icon>{{ data.isDanger ? 'warning_amber' : 'help_outline' }}</mat-icon>
      </div>
      {{ data.title }}
    </div>
    <mat-dialog-content>
      <p class="dlg-msg">{{ data.message }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-stroked-button (click)="ref.close(false)">Cancelar</button>
      <button mat-flat-button [color]="data.isDanger ? 'warn' : 'primary'" (click)="ref.close(true)">
        {{ data.confirmLabel ?? 'Confirmar' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .dlg-title {
      display: flex; align-items: center; gap: 10px;
      font-size: 1rem !important; font-weight: 600;
    }
    .dlg-icon {
      width: 32px; height: 32px; border-radius: 8px;
      background: #eef2ff; display: flex; align-items: center; justify-content: center;
    }
    .dlg-icon mat-icon { color: var(--accent); font-size: 18px !important; width: 18px !important; height: 18px !important; }
    .dlg-icon.danger { background: #fef2f2; }
    .dlg-icon.danger mat-icon { color: var(--danger); }
    .dlg-msg { color: var(--muted); margin: 0; line-height: 1.6; font-size: .9rem; }
  `]
})
export class ConfirmDialogComponent {
  constructor(public ref: MatDialogRef<ConfirmDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData) {}
}
