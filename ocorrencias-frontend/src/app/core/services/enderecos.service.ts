import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { EnderecoResponse, EnderecoRequest, PageResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class EnderecosService {
  private base = `${environment.apiUrl}/api/v1/enderecos`;
  constructor(private http: HttpClient) {}

  listar(page = 0, size = 10) {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<EnderecoResponse>>(this.base, { params });
  }

  criar(body: EnderecoRequest) {
    return this.http.post<EnderecoResponse>(this.base, body);
  }

  atualizar(id: number, body: EnderecoRequest) {
    return this.http.put<EnderecoResponse>(`${this.base}/${id}`, body);
  }

  deletar(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
