package com.humberto.api.ocorrencias.exception;

public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(String mensagem) {
    super(mensagem);
  }
}
