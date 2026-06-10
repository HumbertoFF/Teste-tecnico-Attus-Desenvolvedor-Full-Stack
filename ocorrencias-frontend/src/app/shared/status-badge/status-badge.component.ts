import { Component, Input } from '@angular/core';
import { NgClass } from '@angular/common';
import { StatusOcorrencia } from '../../core/models';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [NgClass],
  template: `<span class="badge" [ngClass]="'badge-' + status?.toLowerCase()">{{ status }}</span>`,
})
export class StatusBadgeComponent {
  @Input() status!: StatusOcorrencia;
}
