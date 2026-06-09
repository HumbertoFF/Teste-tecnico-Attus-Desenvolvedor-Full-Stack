package com.humberto.api.ocorrencias.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FotoResponse {
  private Long codFotoOcorrencia;
  private String urlAcesso;
  private LocalDateTime dtaCriacao;

  public FotoResponse(){}

}
