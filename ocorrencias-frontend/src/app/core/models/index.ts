export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  expiracaoMs: number;
}

export interface ClienteRequest {
  nmeCliente: string;
  dtaNascimento: string;
  nroCpf: string;
}

export interface ClienteResponse {
  codCliente: number;
  nmeCliente: string;
  dtaNascimento: string;
  nroCpf: string;
  dtaCriacao: string;
}

export interface EnderecoRequest {
  nmeLogradouro: string;
  nmeBairro: string;
  nroCep: string;
  nmeCidade: string;
  nmeEstado: string;
}

export interface EnderecoResponse {
  codEndereco: number;
  nmeLogradouro: string;
  nmeBairro: string;
  nroCep: string;
  nmeCidade: string;
  nmeEstado: string;
}

export interface FotoResponse {
  codFotoOcorrencia: number;
  urlAcesso: string;
  dtaCriacao: string;
}

export type StatusOcorrencia = 'ATIVA' | 'FINALIZADA';

export interface OcorrenciaRequest {
  cliente: ClienteRequest;
  endereco: EnderecoRequest;
}

export interface OcorrenciaResponse {
  codOcorrencia: number;
  cliente: ClienteResponse;
  endereco: EnderecoResponse;
  dtaOcorrencia: string;
  staOcorrencia: StatusOcorrencia;
  fotos: FotoResponse[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface OcorrenciaFiltros {
  nmeCliente?: string;
  nroCpf?: string;
  dtaOcorrencia?: string;
  nmeCidade?: string;
  sort?: string;
  page?: number;
  size?: number;
}
