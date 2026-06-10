import { Component, Input } from '@angular/core';
import { NgClass } from '@angular/common';
import { StatusOcorrencia } from '../../core/models';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [NgClass],
  template: `<span class="badge" [ngClass]="cls">{{ status }}</span>`,
  styles: [``]
})
export class StatusBadgeComponent {
  @Input() status!: StatusOcorrencia;
  get cls() { return `badge-${this.status?.toLowerCase()}`; }
}
