package com.humberto.api.ocorrencias.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EnderecoRequest {
  @NotBlank(message = "Logradouro é obrigatório")
  @Size(max = 150)
  private String nmeLogradouro;

  @NotBlank(message = "Bairro é obrigatório")
  @Size(max = 100)
  private String nmeBairro;

  @NotBlank(message = "CEP é obrigatório")
  @Size(min = 8, max = 8, message = "CEP deve conter 8 dígitos")
  private String nroCep;

  @NotBlank(message = "Cidade é obrigatória")
  @Size(max = 100)
  private String nmeCidade;

  @NotBlank(message = "Estado é obrigatório")
  private String nmeEstado;
}
