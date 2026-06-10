import { Component, inject, signal } from '@angular/core';
import {
  FormBuilder, Validators, ReactiveFormsModule,
  AbstractControl, ValidationErrors
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatStepperModule } from '@angular/material/stepper';
import { STEPPER_GLOBAL_OPTIONS } from '@angular/cdk/stepper';
import { OcorrenciasService } from '../../../core/services/ocorrencias.service';
import { UploadZoneComponent } from '../../../shared/upload-zone/upload-zone.component';

const UFS = ['AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO'];

function cpfValidator(c: AbstractControl): ValidationErrors | null {
  const v = (c.value ?? '').replace(/\D/g, '');
  if (v.length !== 11 || /^(\d)\1+$/.test(v)) return { cpf: true };
  let s = 0;
  for (let i = 0; i < 9; i++) s += +v[i] * (10 - i);
  let r = (s * 10) % 11;
  if (r >= 10) r = 0;
  if (r !== +v[9]) return { cpf: true };
  s = 0;
  for (let i = 0; i < 10; i++) s += +v[i] * (11 - i);
  r = (s * 10) % 11;
  if (r >= 10) r = 0;
  return r !== +v[10] ? { cpf: true } : null;
}

@Component({
  selector: 'app-nova',
  standalone: true,
  providers: [{ provide: STEPPER_GLOBAL_OPTIONS, useValue: { showError: true } }],
  imports: [
    RouterLink, ReactiveFormsModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatIconModule, MatProgressSpinnerModule,
    MatStepperModule, UploadZoneComponent
  ],
  template: `
    <div class="page-header">
      <h1>Nova Ocorrência</h1>
      <p>Preencha os dados em 3 etapas para registrar um novo caso</p>
    </div>

    <div class="nova-container">
      <mat-stepper #stepper [linear]="true" class="nova-stepper">

        <!-- ── STEP 1: Cliente ── -->
        <mat-step [stepControl]="clienteForm" label="Dados do Cliente" errorMessage="Corrija os erros">
          <form [formGroup]="clienteForm" novalidate>
            <div class="step-body">
              <div class="section-card">
                <div class="section-title-row">
                  <mat-icon class="stitle-icon">person_outline</mat-icon>
                  <span class="section-title" style="margin:0">Identificação do Cliente</span>
                </div>
                <div class="form-grid-2">
                  <mat-form-field appearance="fill" class="span-2">
                    <mat-label>Nome completo *</mat-label>
                    <input matInput formControlName="nmeCliente" placeholder="Ex: João da Silva" />
                    @if (clienteForm.get('nmeCliente')?.invalid && clienteForm.get('nmeCliente')?.touched) {
                      <mat-error>Nome é obrigatório</mat-error>
                    }
                  </mat-form-field>

                  <mat-form-field appearance="fill">
                    <mat-label>CPF *</mat-label>
                    <mat-icon matPrefix>badge</mat-icon>
                    <input matInput formControlName="nroCpf"
                           placeholder="000.000.000-00"
                           maxlength="14"
                           (input)="maskCpf($event)" />
                    @if (clienteForm.get('nroCpf')?.hasError('required') && clienteForm.get('nroCpf')?.touched) {
                      <mat-error>CPF obrigatório</mat-error>
                    }
                    @if (clienteForm.get('nroCpf')?.hasError('cpf') && clienteForm.get('nroCpf')?.touched) {
                      <mat-error>CPF inválido — verifique os dígitos</mat-error>
                    }
                    <mat-hint>Formato: 000.000.000-00</mat-hint>
                  </mat-form-field>

                  <mat-form-field appearance="fill">
                    <mat-label>Data de nascimento *</mat-label>
                    <mat-icon matPrefix>cake</mat-icon>
                    <input matInput type="date" formControlName="dtaNascimento" />
                    @if (clienteForm.get('dtaNascimento')?.invalid && clienteForm.get('dtaNascimento')?.touched) {
                      <mat-error>Data de nascimento obrigatória</mat-error>
                    }
                  </mat-form-field>
                </div>
              </div>
            </div>
            <div class="step-actions">
              <a mat-stroked-button routerLink="/ocorrencias">Cancelar</a>
              <button mat-flat-button color="primary" matStepperNext
                      (click)="clienteForm.markAllAsTouched()">
                Próximo <mat-icon>arrow_forward</mat-icon>
              </button>
            </div>
          </form>
        </mat-step>

        <!-- ── STEP 2: Endereço ── -->
        <mat-step [stepControl]="enderecoForm" label="Endereço" errorMessage="Corrija os erros">
          <form [formGroup]="enderecoForm" novalidate>
            <div class="step-body">
              <div class="section-card">
                <div class="section-title-row">
                  <mat-icon class="stitle-icon">location_on</mat-icon>
                  <span class="section-title" style="margin:0">Local da Ocorrência</span>
                </div>
                <div class="form-grid-2">
                  <mat-form-field appearance="fill" class="span-2">
                    <mat-label>Logradouro *</mat-label>
                    <input matInput formControlName="nmeLogradouro" placeholder="Ex: Rua das Flores, 123" />
                    @if (enderecoForm.get('nmeLogradouro')?.invalid && enderecoForm.get('nmeLogradouro')?.touched) {
                      <mat-error>Logradouro obrigatório</mat-error>
                    }
                  </mat-form-field>

                  <mat-form-field appearance="fill">
                    <mat-label>Bairro *</mat-label>
                    <input matInput formControlName="nmeBairro" />
                    @if (enderecoForm.get('nmeBairro')?.invalid && enderecoForm.get('nmeBairro')?.touched) {
                      <mat-error>Bairro obrigatório</mat-error>
                    }
                  </mat-form-field>

                  <mat-form-field appearance="fill">
                    <mat-label>CEP *</mat-label>
                    <input matInput formControlName="nroCep" maxlength="8"
                           placeholder="Somente números"
                           (input)="onlyNumbers($event)" />
                    @if (enderecoForm.get('nroCep')?.hasError('required') && enderecoForm.get('nroCep')?.touched) {
                      <mat-error>CEP obrigatório</mat-error>
                    }
                    @if (enderecoForm.get('nroCep')?.hasError('pattern') && enderecoForm.get('nroCep')?.touched) {
                      <mat-error>CEP deve ter exatamente 8 dígitos</mat-error>
                    }
                    <mat-hint>Apenas 8 dígitos, sem hífen</mat-hint>
                  </mat-form-field>

                  <mat-form-field appearance="fill">
                    <mat-label>Cidade *</mat-label>
                    <input matInput formControlName="nmeCidade" />
                    @if (enderecoForm.get('nmeCidade')?.invalid && enderecoForm.get('nmeCidade')?.touched) {
                      <mat-error>Cidade obrigatória</mat-error>
                    }
                  </mat-form-field>

                  <mat-form-field appearance="fill">
                    <mat-label>Estado *</mat-label>
                    <mat-select formControlName="nmeEstado">
                      @for (uf of ufs; track uf) {
                        <mat-option [value]="uf">{{ uf }}</mat-option>
                      }
                    </mat-select>
                    @if (enderecoForm.get('nmeEstado')?.invalid && enderecoForm.get('nmeEstado')?.touched) {
                      <mat-error>Estado obrigatório</mat-error>
                    }
                  </mat-form-field>
                </div>
              </div>
            </div>
            <div class="step-actions">
              <button mat-stroked-button matStepperPrevious>
                <mat-icon>arrow_back</mat-icon> Voltar
              </button>
              <button mat-flat-button color="primary" matStepperNext
                      (click)="enderecoForm.markAllAsTouched()">
                Próximo <mat-icon>arrow_forward</mat-icon>
              </button>
            </div>
          </form>
        </mat-step>

        <!-- ── STEP 3: Arquivos + Revisão ── -->
        <mat-step label="Arquivos e Confirmação">
          <div class="step-body">
            <div class="section-card">
              <div class="section-title-row">
                <mat-icon class="stitle-icon">cloud_upload</mat-icon>
                <span class="section-title" style="margin:0">Arquivos Anexos
                  <span class="optional-tag">opcional</span>
                </span>
              </div>
              <app-upload-zone (filesChange)="files = $event"
                               hint="PNG, JPG, WEBP, GIF, PDF — máx 10MB por arquivo" />
            </div>

            <div class="section-card review-card">
              <div class="section-title-row">
                <mat-icon class="stitle-icon">fact_check</mat-icon>
                <span class="section-title" style="margin:0">Revisão dos Dados</span>
              </div>
              <div class="review-grid">
                <div class="review-col">
                  <p class="review-group-label">Cliente</p>
                  <div class="rv"><span class="rv-l">Nome</span><span>{{ clienteForm.value.nmeCliente }}</span></div>
                  <div class="rv"><span class="rv-l">CPF</span><span class="mono">{{ clienteForm.value.nroCpf }}</span></div>
                  <div class="rv"><span class="rv-l">Nascimento</span><span>{{ clienteForm.value.dtaNascimento }}</span></div>
                </div>
                <div class="review-col">
                  <p class="review-group-label">Endereço</p>
                  <div class="rv"><span class="rv-l">Cidade / UF</span><span>{{ enderecoForm.value.nmeCidade }} / {{ enderecoForm.value.nmeEstado }}</span></div>
                  <div class="rv"><span class="rv-l">CEP</span><span class="mono">{{ enderecoForm.value.nroCep }}</span></div>
                  <div class="rv"><span class="rv-l">Logradouro</span><span>{{ enderecoForm.value.nmeLogradouro }}</span></div>
                  <div class="rv"><span class="rv-l">Arquivos</span><span>{{ files.length }} selecionado(s)</span></div>
                </div>
              </div>
            </div>

            @if (error()) {
              <div class="form-error">
                <mat-icon>warning_amber</mat-icon>
                {{ error() }}
              </div>
            }
          </div>
          <div class="step-actions">
            <button mat-stroked-button matStepperPrevious>
              <mat-icon>arrow_back</mat-icon> Voltar
            </button>
            <button mat-flat-button color="primary" (click)="submit()" [disabled]="loading()">
              @if (loading()) { <mat-spinner diameter="18" style="margin-right:6px" /> }
              @else { <mat-icon>save</mat-icon> }
              Cadastrar Ocorrência
            </button>
          </div>
        </mat-step>

      </mat-stepper>
    </div>
  `,
  styles: [`
    .nova-container { max-width: 820px; }

    /* Stepper body */
    .step-body { padding: 20px 0 4px; }
    .step-actions {
      display: flex; gap: 10px; align-items: center;
      padding: 4px 0 8px; flex-wrap: wrap;
    }

    /* Section title row */
    .section-title-row {
      display: flex; align-items: center; gap: 8px; margin-bottom: 16px;
    }
    .stitle-icon {
      font-size: 18px !important; width: 18px !important; height: 18px !important;
      color: var(--accent);
    }

    .optional-tag {
      font-size: .7rem; color: var(--muted);
      background: var(--surface2); padding: 2px 7px; border-radius: 20px;
      margin-left: 8px; font-weight: 500; vertical-align: middle;
    }

    .mono { font-family: 'JetBrains Mono', monospace; font-size: .84rem; }

    /* Review card */
    .review-card { background: var(--surface2) !important; }
    .review-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 24px; }
    @media(max-width:600px) { .review-grid { grid-template-columns: 1fr; } }
    .review-group-label {
      font-size: .75rem; font-weight: 600; color: var(--accent);
      text-transform: uppercase; letter-spacing: .5px;
      margin: 0 0 8px;
    }
    .review-col { padding: 4px 0; }
    .rv {
      display: flex; justify-content: space-between; align-items: baseline;
      gap: 8px; padding: 6px 0; border-bottom: 1px solid var(--border);
      font-size: .86rem;
    }
    .rv:last-child { border-bottom: none; }
    .rv-l { color: var(--muted); font-size: .8rem; white-space: nowrap; flex-shrink: 0; }

    /* Error */
    .form-error {
      display: flex; align-items: flex-start; gap: 10px;
      background: rgba(224,92,92,.1); border: 1px solid var(--danger);
      color: #f09090; padding: 12px 16px; border-radius: 8px;
      font-size: .88rem; margin-bottom: 12px;
    }
    .form-error mat-icon { flex-shrink: 0; }
  `]
})
export class NovaComponent {
  private fb     = inject(FormBuilder);
  private svc    = inject(OcorrenciasService);
  private router = inject(Router);
  private snack  = inject(MatSnackBar);

  ufs    = UFS;
  files: File[] = [];
  loading = signal(false);
  error   = signal('');

  clienteForm = this.fb.group({
    nmeCliente:    ['', Validators.required],
    nroCpf:        ['', [Validators.required, cpfValidator]],
    dtaNascimento: ['', Validators.required],
  });

  enderecoForm = this.fb.group({
    nmeLogradouro: ['', Validators.required],
    nmeBairro:     ['', Validators.required],
    nroCep:        ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    nmeCidade:     ['', Validators.required],
    nmeEstado:     ['', Validators.required],
  });

  maskCpf(event: Event) {
    const input = event.target as HTMLInputElement;
    let v = input.value.replace(/\D/g, '').slice(0, 11);
    if      (v.length > 9) v = v.replace(/^(\d{3})(\d{3})(\d{3})(\d{1,2})/, '$1.$2.$3-$4');
    else if (v.length > 6) v = v.replace(/^(\d{3})(\d{3})(\d{1,3})/, '$1.$2.$3');
    else if (v.length > 3) v = v.replace(/^(\d{3})(\d{1,3})/, '$1.$2');
    this.clienteForm.get('nroCpf')!.setValue(v, { emitEvent: false });
    input.value = v;
  }

  onlyNumbers(event: Event) {
    const input = event.target as HTMLInputElement;
    const clean = input.value.replace(/\D/g, '');
    this.enderecoForm.get('nroCep')!.setValue(clean, { emitEvent: false });
    input.value = clean;
  }

  submit() {
    if (this.clienteForm.invalid || this.enderecoForm.invalid) return;
    this.loading.set(true);
    this.error.set('');

    const cv = this.clienteForm.value;
    const ev = this.enderecoForm.value;

    this.svc.cadastrar({
      cliente: {
        nmeCliente: cv.nmeCliente!,
        dtaNascimento: cv.dtaNascimento!,
        nroCpf: cv.nroCpf!
      },
      endereco: {
        nmeLogradouro: ev.nmeLogradouro!, nmeBairro: ev.nmeBairro!,
        nroCep: ev.nroCep!, nmeCidade: ev.nmeCidade!, nmeEstado: ev.nmeEstado!
      }
    }, this.files).subscribe({
      next: res => {
        this.snack.open(`Ocorrência #${res.codOcorrencia} cadastrada com sucesso!`, '', {
          duration: 4000, panelClass: 'success-snack'
        });
        this.router.navigate(['/ocorrencias', res.codOcorrencia]);
      },
      error: e => {
        this.loading.set(false);
        const campos = e.error?.campos;
        this.error.set(campos
          ? 'Erros de validação: ' + Object.entries(campos).map(([k, v]) => `${k}: ${v}`).join(' | ')
          : (e.error?.detail ?? 'Erro ao cadastrar a ocorrência. Tente novamente.')
        );
      }
    });
  }
}
