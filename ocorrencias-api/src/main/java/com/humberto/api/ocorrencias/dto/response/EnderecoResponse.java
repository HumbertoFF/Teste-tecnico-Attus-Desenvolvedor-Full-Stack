package com.humberto.api.ocorrencias.dto.response;

import com.humberto.api.ocorrencias.model.Endereco;
import lombok.Data;

@Data
public class EnderecoResponse {

  private Long codEndereco;
  private String nmeLogradouro;
  private String nmeBairro;
  private String nroCep;
  private String nmeCidade;
  private String nmeEstado;

  private EnderecoResponse() {}

  public static EnderecoResponse from(Endereco e) {
    EnderecoResponse r = new EnderecoResponse();
    r.codEndereco = e.getCodEndereco();
    r.nmeLogradouro = e.getNmeLogradouro();
    r.nmeBairro = e.getNmeBairro();
    r.nroCep = e.getNroCep();
    r.nmeCidade = e.getNmeCidade();
    r.nmeEstado = e.getNmeEstado();
    return r;
  }
}
