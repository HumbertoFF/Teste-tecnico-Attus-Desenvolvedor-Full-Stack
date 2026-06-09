package com.humberto.api.ocorrencias.controller;

import com.humberto.api.ocorrencias.dto.request.ClienteRequest;
import com.humberto.api.ocorrencias.dto.response.ClienteResponse;
import com.humberto.api.ocorrencias.service.ClienteService;
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
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes")
public class ClienteController {

  private final ClienteService clienteService;

  @GetMapping
  @Operation(summary = "Listar clientes paginado")
  public ResponseEntity<Page<ClienteResponse>> listar(
    @ParameterObject @PageableDefault(size = 20, sort = "nmeCliente") Pageable pageable) {
    return ResponseEntity.ok(clienteService.listar(pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar cliente por ID")
  public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id){
    return ResponseEntity.ok(clienteService.buscarPorId(id));
  }

  @PostMapping
  @Operation(summary = "Criar cliente")
  public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request){
    ClienteResponse response = clienteService.criar(request);
    var location = ServletUriComponentsBuilder.fromCurrentRequest()
      .path("/{id}").buildAndExpand(response.getCodCliente()).toUri();
    return ResponseEntity.created(location).body(response);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar cliente")
  public ResponseEntity<ClienteResponse> atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
    return ResponseEntity.ok(clienteService.atualizar(id,request));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover Cliente")
  public ResponseEntity<Void> deletar(@PathVariable Long id) {
    clienteService.deletar(id);
    return ResponseEntity.noContent().build();
  }
}
