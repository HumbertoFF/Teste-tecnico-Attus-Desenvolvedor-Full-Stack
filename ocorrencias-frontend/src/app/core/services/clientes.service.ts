import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ClienteResponse, ClienteRequest, PageResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class ClientesService {
  private base = `${environment.apiUrl}/api/v1/clientes`;
  constructor(private http: HttpClient) {}

  listar(page = 0, size = 10) {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', 'nmeCliente,asc');
    return this.http.get<PageResponse<ClienteResponse>>(this.base, { params });
  }

  buscarPorId(id: number) {
    return this.http.get<ClienteResponse>(`${this.base}/${id}`);
  }

  criar(body: ClienteRequest) {
    return this.http.post<ClienteResponse>(this.base, body);
  }

  atualizar(id: number, body: ClienteRequest) {
    return this.http.put<ClienteResponse>(`${this.base}/${id}`, body);
  }

  deletar(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
