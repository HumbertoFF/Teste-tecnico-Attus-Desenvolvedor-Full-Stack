package com.humberto.api.ocorrencias.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OcorrenciaRequest {

  @Valid
  @NotNull(message = "Dados do cliente são obrigatórios")
  private ClienteRequest cliente;

  @Valid
  @NotNull(message = "Dados do endereço são obrigatórios")
  private EnderecoRequest endereco;
}
