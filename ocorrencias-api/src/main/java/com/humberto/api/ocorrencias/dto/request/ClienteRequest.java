package com.humberto.api.ocorrencias.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.br.CPF;
import java.time.LocalDate;

@Data
public class ClienteRequest {
  @NotBlank(message = "Nome é obrigatório")
  @Size(max = 100)
  private String nmeCliente;

  @NotNull(message = "Data de nascimento é obrigatória")
  private LocalDate dtaNascimento;

  @CPF(message = "CPF inválido")
  @NotBlank(message = "CPF é obrigatório")
  private String nroCpf;
}
