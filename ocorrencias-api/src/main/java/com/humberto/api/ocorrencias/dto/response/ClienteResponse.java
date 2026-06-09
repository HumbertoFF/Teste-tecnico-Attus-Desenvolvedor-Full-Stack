package com.humberto.api.ocorrencias.dto.response;

import com.humberto.api.ocorrencias.model.Cliente;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ClienteResponse {
  private Long codCliente;
  private String nmeCliente;
  private LocalDate dtaNascimento;
  private String nroCpf;
  private LocalDateTime dtaCriacao;

  public static ClienteResponse from(Cliente c) {
     ClienteResponse r = new ClienteResponse();
     r.codCliente = c.getCodCliente();
     r.nmeCliente = c.getNmeCliente();
     r.dtaNascimento = c.getDtaNascimento();
     r.nroCpf = c.getNroCpf();
     r.dtaCriacao = c.getDtaCriacao();
     return r;
  }
}
