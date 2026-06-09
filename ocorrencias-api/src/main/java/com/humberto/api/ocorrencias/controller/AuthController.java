package com.humberto.api.ocorrencias.controller;

import com.humberto.api.ocorrencias.dto.request.LoginRequest;
import com.humberto.api.ocorrencias.dto.response.LoginResponse;
import com.humberto.api.ocorrencias.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  @Operation(summary = "Autenticar usuário e obter token JWT (expira em 30 min)")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.autenticar(request));
  }
}
