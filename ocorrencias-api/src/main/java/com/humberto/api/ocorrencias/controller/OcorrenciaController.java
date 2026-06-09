package com.humberto.api.ocorrencias.controller;

import com.humberto.api.ocorrencias.dto.request.OcorrenciaRequest;
import com.humberto.api.ocorrencias.dto.response.FotoResponse;
import com.humberto.api.ocorrencias.dto.response.OcorrenciaResponse;
import com.humberto.api.ocorrencias.service.FotoOcorrenciaService;
import com.humberto.api.ocorrencias.service.OcorrenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ocorrencias")
@RequiredArgsConstructor
@Tag(name = "Ocorrências", description = "Gerenciamento de ocorrências")
public class OcorrenciaController {

  private final OcorrenciaService ocorrenciaService;
  private final FotoOcorrenciaService fotoOcorrenciaService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
          summary = "Cadastrar ocorrência",
          description = "Cadastra uma nova ocorrência com dados do cliente, endereço e arquivos anexos opcionais (imagens, PDFs). " +
                  "Enviar como multipart/form-data: campo 'dados' com o JSON e campo 'fotos' com os arquivos.")
  public ResponseEntity<OcorrenciaResponse> cadastrar(
    @RequestPart("dados") @Valid OcorrenciaRequest request,
    @RequestPart(value = "arquivos", required = false) List<MultipartFile> fotos) {

    OcorrenciaResponse response = ocorrenciaService.cadastrar(request, fotos);
    var location = ServletUriComponentsBuilder.fromCurrentRequest()
      .path("/{id}").buildAndExpand(response.getCodOcorrencia()).toUri();
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping
  @Operation(
          summary = "Listar ocorrências",
          description = "Retorna ocorrências paginadas com filtros opcionais. " +
                  "Campos ordenáveis: dtaOcorrencia, nmeCidade. Direções: asc, desc.")
  public ResponseEntity<Page<OcorrenciaResponse>> listar(
    @Parameter(description = "Filtrar por nome do cliente")
    @RequestParam(required = false) String nmeCliente,

    @Parameter(description = "Filtrar por CPF do cliente")
    @RequestParam(required = false) String nroCpf,

    @Parameter(description = "Filtrar por data da ocorrência (yyyy-MM-dd)")
    @RequestParam(required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtaOcorrencia,

    @Parameter(description = "Filtrar por cidade da ocorrência")
    @RequestParam(required = false) String nmeCidade,

    @ParameterObject @PageableDefault(size = 20, sort = "dtaOcorrencia")
    Pageable pageable) {
      return ResponseEntity.ok(
        ocorrenciaService.listar(nmeCliente, nroCpf, dtaOcorrencia, nmeCidade, pageable)
      );
  }

  @GetMapping("/{id}")
  @Operation(
          summary = "Buscar ocorrência por ID",
          description = "Retorna os dados completos da ocorrência incluindo cliente, endereço e URLs de acesso aos arquivos anexados.")
  public ResponseEntity<OcorrenciaResponse> buscarPorId(@PathVariable Long id) {
    return ResponseEntity.ok(ocorrenciaService.buscarPorId(id));
  }

  @PostMapping(value = "/{id}/arquivos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
          summary = "Anexar arquivos à ocorrência",
          description = "Adiciona um ou mais arquivos (imagens, PDFs, documentos) a uma ocorrência existente. " +
              "Bloqueado se a ocorrência já estiver FINALIZADA. " +
              "Os arquivos são armazenados em volume persistente e as URLs de acesso são retornadas.")
  public ResponseEntity<List<FotoResponse>> adicionarFotos(
    @PathVariable Long id,
    @RequestPart("arquivos") List<MultipartFile> fotos) {

    List<FotoResponse> response = fotoOcorrenciaService.adicionarFotos(id, fotos);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{id}/finalizar")
  @Operation(
          summary = "Finalizar ocorrência",
          description = "Altera o status da ocorrência de ATIVA para FINALIZADA. " +
                  "Uma vez finalizada, a ocorrência não pode ser editada, não aceita novos arquivos e não pode ser removida."
  )
  public ResponseEntity<OcorrenciaResponse> finalizar(@PathVariable Long id) {
    return ResponseEntity.ok(ocorrenciaService.finalizar(id));
  }

  @DeleteMapping("/{id}")
  @Operation(
          summary = "Remover ocorrência",
          description = "Remove permanentemente uma ocorrência. Bloqueado se o status for FINALIZADA.")
  public ResponseEntity<Void> deletar(@PathVariable Long id) {
    ocorrenciaService.deletar(id);
    return ResponseEntity.noContent().build();
  }
}
