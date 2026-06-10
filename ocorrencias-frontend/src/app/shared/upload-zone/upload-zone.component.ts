import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-upload-zone',
  standalone: true,
  imports: [NgFor, MatIconModule, MatChipsModule],
  template: `
    <div class="upload-zone" (click)="fi.click()"
         (dragover)="$event.preventDefault()" (drop)="onDrop($event)">
      <input #fi type="file" multiple [accept]="accept" style="display:none" (change)="onSelect($event)" />
      <mat-icon class="up-icon">cloud_upload</mat-icon>
      <p class="up-text">Arraste ou <strong>clique para selecionar</strong></p>
      <p class="up-hint">{{ hint }}</p>
    </div>
    @if (files.length) {
      <div class="chip-list">
        @for (file of files; track file.name; let i = $index) {
          <mat-chip-row (removed)="remove(i)">
            {{ file.name }}
            <span class="chip-size">({{ (file.size/1024).toFixed(0) }} KB)</span>
            <button matChipRemove><mat-icon>cancel</mat-icon></button>
          </mat-chip-row>
        }
      </div>
    }
  `,
  styles: [`
    .upload-zone {
      border: 2px dashed var(--border); border-radius: 10px;
      padding: 28px 20px; text-align: center; cursor: pointer;
      background: var(--surface2);
      transition: border-color .15s, background .15s;
    }
    .upload-zone:hover { border-color: var(--accent); background: var(--accent-dim); }
    .up-icon  { font-size: 32px !important; width: 32px !important; height: 32px !important; color: var(--muted); }
    .up-text  { margin: 8px 0 3px; font-size: .88rem; color: var(--text2); }
    .up-hint  { margin: 0; font-size: .76rem; color: var(--muted); }
    .chip-list { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px; }
    .chip-size { margin-left: 6px; color: var(--muted); font-size: .72rem; }
  `]
})
export class UploadZoneComponent {
  @Input() accept = 'image/*,.pdf';
  @Input() hint   = 'PNG, JPG, PDF — máx 10MB cada';
  @Output() filesChange = new EventEmitter<File[]>();
  files: File[] = [];

  onSelect(e: Event) {
    this.addFiles(Array.from((e.target as HTMLInputElement).files ?? []));
    (e.target as HTMLInputElement).value = '';
  }
  onDrop(e: DragEvent) { e.preventDefault(); this.addFiles(Array.from(e.dataTransfer?.files ?? [])); }
  addFiles(list: File[]) { this.files = [...this.files, ...list]; this.filesChange.emit(this.files); }
  remove(i: number) { this.files.splice(i, 1); this.files = [...this.files]; this.filesChange.emit(this.files); }
}
