import { Component, Inject, inject, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ClientesService } from '../../../core/services/clientes.service';
import { ClienteResponse } from '../../../core/models';

function cpfValidator(c: AbstractControl): ValidationErrors | null {
  const v = (c.value ?? '').replace(/\D/g, '');
  if (v.length !== 11 || /^(\d)\1+$/.test(v)) return { cpf: true };
  let s = 0; for (let i = 0; i < 9; i++) s += +v[i] * (10 - i);
  let r = (s * 10) % 11; if (r >= 10) r = 0; if (r !== +v[9]) return { cpf: true };
  s = 0; for (let i = 0; i < 10; i++) s += +v[i] * (11 - i);
  r = (s * 10) % 11; if (r >= 10) r = 0;
  return r !== +v[10] ? { cpf: true } : null;
}

@Component({
  selector: 'app-cliente-form',
  standalone: true,
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatProgressSpinnerModule],
  template: `
    <h2 mat-dialog-title>{{ data ? 'Editar Cliente' : 'Novo Cliente' }}</h2>
    <mat-dialog-content>
      @if (error()) {
        <div class="form-error">{{ error() }}</div>
      }
      <form [formGroup]="form" class="form-col">
        <mat-form-field appearance="fill">
          <mat-label>Nome completo *</mat-label>
          <input matInput formControlName="nmeCliente" />
          @if (form.get('nmeCliente')?.hasError('required') && form.get('nmeCliente')?.touched) {
            <mat-error>Nome obrigatório</mat-error>
          }
        </mat-form-field>
        <mat-form-field appearance="fill">
          <mat-label>CPF *</mat-label>
          <input matInput formControlName="nroCpf" placeholder="000.000.000-00" maxlength="14" (input)="maskCpf($event)" />
          @if (form.get('nroCpf')?.hasError('required') && form.get('nroCpf')?.touched) {
            <mat-error>CPF obrigatório</mat-error>
          }
          @if (form.get('nroCpf')?.hasError('cpf') && form.get('nroCpf')?.touched) {
            <mat-error>CPF inválido</mat-error>
          }
        </mat-form-field>
        <mat-form-field appearance="fill">
          <mat-label>Data de nascimento *</mat-label>
          <input matInput type="date" formControlName="dtaNascimento" />
          @if (form.get('dtaNascimento')?.hasError('required') && form.get('dtaNascimento')?.touched) {
            <mat-error>Data obrigatória</mat-error>
          }
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="ref.close(false)">Cancelar</button>
      <button mat-flat-button color="primary" (click)="submit()" [disabled]="loading()">
        @if (loading()) { <mat-spinner diameter="16" /> }
        Salvar
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .form-col { display: flex; flex-direction: column; gap: 4px; padding-top: 4px; min-width: 360px; }
    .form-error { background: rgba(224,92,92,.12); border: 1px solid var(--danger); color: #f09090; padding: 10px 14px; border-radius: 8px; font-size: .86rem; margin-bottom: 8px; }
    mat-form-field { width: 100%; }
  `]
})
export class FormComponent {
  private fb  = inject(FormBuilder);
  private svc = inject(ClientesService);
  loading = signal(false);
  error   = signal('');

  form = this.fb.group({
    nmeCliente:    [this.data?.nmeCliente ?? '', Validators.required],
    nroCpf:        ['', [Validators.required, cpfValidator]],
    dtaNascimento: [this.data?.dtaNascimento ?? '', Validators.required],
  });

  constructor(public ref: MatDialogRef<FormComponent>, @Inject(MAT_DIALOG_DATA) public data: ClienteResponse | null) {
    if (data) {
      const cpf = data.nroCpf.replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, '$1.$2.$3-$4');
      this.form.patchValue({ nroCpf: cpf, dtaNascimento: data.dtaNascimento });
    }
  }

  maskCpf(event: Event) {
    const input = event.target as HTMLInputElement;
    let v = input.value.replace(/\D/g, '').slice(0, 11);
    if (v.length > 9) v = v.replace(/^(\d{3})(\d{3})(\d{3})(\d{1,2})/, '$1.$2.$3-$4');
    else if (v.length > 6) v = v.replace(/^(\d{3})(\d{3})(\d{1,3})/, '$1.$2.$3');
    else if (v.length > 3) v = v.replace(/^(\d{3})(\d{1,3})/, '$1.$2');
    this.form.get('nroCpf')!.setValue(v, { emitEvent: false });
    input.value = v;
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.error.set('');
    const v = this.form.value;
    const body = { nmeCliente: v.nmeCliente!, nroCpf: v.nroCpf!, dtaNascimento: v.dtaNascimento! };
    const req$ = this.data ? this.svc.atualizar(this.data.codCliente, body) : this.svc.criar(body);
    req$.subscribe({
      next: () => { this.ref.close(true); },
      error: e => { this.loading.set(false); this.error.set(e.error?.detail ?? JSON.stringify(e.error?.campos ?? 'Erro')); }
    });
  }
}
