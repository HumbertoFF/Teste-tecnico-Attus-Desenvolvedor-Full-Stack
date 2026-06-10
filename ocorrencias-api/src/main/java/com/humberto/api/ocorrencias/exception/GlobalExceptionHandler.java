package com.humberto.api.ocorrencias.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("https://api.ocorrencias.com/errors/not-found"));
        return pd;
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setType(URI.create("https://api.ocorrencias.com/errors/business"));
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erros = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        f -> f.getDefaultMessage() != null ? f.getDefaultMessage() : "inválido"
                ));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Dados inválidos");
        pd.setType(URI.create("https://api.ocorrencias.com/errors/validation"));
        pd.setProperty("campos", erros);
        return pd;
    }

    /**
     * Cliente fechou a conexão antes de receber a resposta completa.
     * Acontece normalmente com healthchecks que expiram (timeout do Docker)
     * ou quando o browser cancela a requisição. Não é um erro da aplicação.
     */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleClientAbort(AsyncRequestNotUsableException ex) {
        log.debug("Conexão encerrada pelo cliente antes da resposta ser enviada: {}", ex.getMessage());
        // Sem retorno — resposta já não pode ser escrita
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        // Filtra ClientAbortException que pode chegar sem ser AsyncRequestNotUsableException
        if (isClientAbort(ex)) {
            log.debug("Conexão encerrada pelo cliente: {}", ex.getMessage());
            return null;
        }
        log.error("Erro inesperado", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
        pd.setType(URI.create("https://api.ocorrencias.com/errors/internal"));
        return pd;
    }

    private boolean isClientAbort(Throwable ex) {
        Throwable cause = ex;
        while (cause != null) {
            String name = cause.getClass().getName();
            if (name.contains("ClientAbortException") || name.contains("EofException")) {
                return true;
            }
            String msg = cause.getMessage();
            if (msg != null && (msg.contains("Connection reset by peer") || msg.contains("Broken pipe"))) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}