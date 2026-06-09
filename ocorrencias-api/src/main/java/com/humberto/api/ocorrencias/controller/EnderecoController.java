package com.humberto.api.ocorrencias.controller;

import com.humberto.api.ocorrencias.dto.request.EnderecoRequest;
import com.humberto.api.ocorrencias.dto.response.EnderecoResponse;
import com.humberto.api.ocorrencias.service.EnderecoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/enderecos")
@RequiredArgsConstructor
@Tag(name = "Endereços")
public class EnderecoController {

  private final EnderecoService enderecoService;

  @GetMapping
  @Operation(summary = "Listar endereços paginado")
  public ResponseEntity<Page<EnderecoResponse>> listar(
    @ParameterObject @PageableDefault(size = 20, sort = "nmeCidade") Pageable pageable) {
   return ResponseEntity.ok(enderecoService.listar(pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar endereço por ID")
  public ResponseEntity<EnderecoResponse> buscarPorId(@PathVariable Long id) {
    return ResponseEntity.ok(enderecoService.buscarPorId(id));
  }

  @PostMapping
  @Operation(summary = "Criar endereço")
  public ResponseEntity<EnderecoResponse> criar(@Valid @RequestBody EnderecoRequest request) {
    EnderecoResponse response = enderecoService.criar(request);
    var location = ServletUriComponentsBuilder.fromCurrentRequest()
      .path("/{id}").buildAndExpand(response.getCodEndereco()).toUri();
    return ResponseEntity.created(location).body(response);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar endereço")
  public ResponseEntity<EnderecoResponse> atualizar(
    @PathVariable Long id,
    @Valid @RequestBody EnderecoRequest request) {
    return ResponseEntity.ok(enderecoService.atualizar(id, request));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover endereço")
  public ResponseEntity<Void> deletar(@PathVariable Long id) {
    enderecoService.deletar(id);
    return ResponseEntity.noContent().build();
  }
}
