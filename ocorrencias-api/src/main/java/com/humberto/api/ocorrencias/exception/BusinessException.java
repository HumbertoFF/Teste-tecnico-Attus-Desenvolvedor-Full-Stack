package com.humberto.api.ocorrencias.exception;

public class BusinessException extends RuntimeException {
  public BusinessException(String mensagem) {
    super(mensagem);
  }
}

