import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import {
  OcorrenciaResponse, OcorrenciaRequest, PageResponse, FotoResponse, OcorrenciaFiltros
} from '../models';

@Injectable({ providedIn: 'root' })
export class OcorrenciasService {
  private base = `${environment.apiUrl}/api/v1/ocorrencias`;
  constructor(private http: HttpClient) {}

  listar(filtros: OcorrenciaFiltros = {}) {
    let params = new HttpParams()
      .set('page', filtros.page ?? 0)
      .set('size', filtros.size ?? 10)
      .set('sort', filtros.sort ?? 'dtaOcorrencia,desc');
    if (filtros.nmeCliente) params = params.set('nmeCliente', filtros.nmeCliente);
    if (filtros.nroCpf)     params = params.set('nroCpf', filtros.nroCpf);
    if (filtros.dtaOcorrencia) params = params.set('dtaOcorrencia', filtros.dtaOcorrencia);
    if (filtros.nmeCidade)  params = params.set('nmeCidade', filtros.nmeCidade);
    return this.http.get<PageResponse<OcorrenciaResponse>>(this.base, { params });
  }

  buscarPorId(id: number) {
    return this.http.get<OcorrenciaResponse>(`${this.base}/${id}`);
  }

  cadastrar(dados: OcorrenciaRequest, arquivos?: File[]) {
    const form = new FormData();
    form.append('dados', new Blob([JSON.stringify(dados)], { type: 'application/json' }));
    arquivos?.forEach(f => form.append('arquivos', f));
    return this.http.post<OcorrenciaResponse>(this.base, form);
  }

  finalizar(id: number) {
    return this.http.patch<OcorrenciaResponse>(`${this.base}/${id}/finalizar`, {});
  }

  deletar(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  adicionarArquivos(id: number, arquivos: File[]) {
    const form = new FormData();
    arquivos.forEach(f => form.append('arquivos', f));
    return this.http.post<FotoResponse[]>(`${this.base}/${id}/arquivos`, form);
  }
}
