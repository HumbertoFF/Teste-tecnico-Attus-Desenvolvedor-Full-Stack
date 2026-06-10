import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-upload-zone',
  standalone: true,
  imports: [NgFor, MatIconModule, MatChipsModule],
  template: `
    <div class="upload-zone" (click)="fileInput.click()" (dragover)="$event.preventDefault()" (drop)="onDrop($event)">
      <input #fileInput type="file" multiple [accept]="accept" style="display:none" (change)="onSelect($event)" />
      <mat-icon class="upload-icon">cloud_upload</mat-icon>
      <p class="upload-label">Arraste arquivos ou <strong>clique para selecionar</strong></p>
      <p class="upload-hint">{{ hint }}</p>
    </div>
    @if (files.length) {
      <div class="file-chips">
        @for (file of files; track file.name; let i = $index) {
          <mat-chip-row (removed)="remove(i)">
            {{ file.name }}
            <span class="file-size">({{ (file.size / 1024).toFixed(0) }} KB)</span>
            <button matChipRemove><mat-icon>cancel</mat-icon></button>
          </mat-chip-row>
        }
      </div>
    }
  `,
  styles: [`
    .upload-zone {
      border: 2px dashed var(--border);
      border-radius: 10px;
      padding: 28px 20px;
      text-align: center;
      cursor: pointer;
      transition: border-color .2s, background .2s;
    }
    .upload-zone:hover { border-color: var(--accent); background: var(--accent-dim); }
    .upload-icon { font-size: 36px !important; width: 36px !important; height: 36px !important; color: var(--muted); }
    .upload-label { margin: 8px 0 4px; font-size: .92rem; color: var(--text); }
    .upload-hint  { margin: 0; font-size: .78rem; color: var(--muted); }
    .file-chips { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 14px; }
    .file-size  { margin-left: 6px; color: var(--muted); font-size: .75rem; }
  `]
})
export class UploadZoneComponent {
  @Input() accept = 'image/*,.pdf';
  @Input() hint   = 'PNG, JPG, PDF — máx 10MB cada';
  @Output() filesChange = new EventEmitter<File[]>();

  files: File[] = [];

  onSelect(event: Event) {
    const input = event.target as HTMLInputElement;
    this.addFiles(Array.from(input.files ?? []));
    input.value = '';
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.addFiles(Array.from(event.dataTransfer?.files ?? []));
  }

  addFiles(list: File[]) {
    this.files = [...this.files, ...list];
    this.filesChange.emit(this.files);
  }

  remove(i: number) {
    this.files.splice(i, 1);
    this.files = [...this.files];
    this.filesChange.emit(this.files);
  }
}
