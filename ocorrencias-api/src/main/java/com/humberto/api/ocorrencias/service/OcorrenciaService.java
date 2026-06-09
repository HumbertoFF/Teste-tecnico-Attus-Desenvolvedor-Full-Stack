package com.humberto.api.ocorrencias.service;

import com.humberto.api.ocorrencias.dto.request.OcorrenciaRequest;
import com.humberto.api.ocorrencias.dto.response.ClienteResponse;
import com.humberto.api.ocorrencias.dto.response.EnderecoResponse;
import com.humberto.api.ocorrencias.dto.response.FotoResponse;
import com.humberto.api.ocorrencias.dto.response.OcorrenciaResponse;
import com.humberto.api.ocorrencias.exception.BusinessException;
import com.humberto.api.ocorrencias.exception.ResourceNotFoundException;
import com.humberto.api.ocorrencias.model.Cliente;
import com.humberto.api.ocorrencias.model.Endereco;
import com.humberto.api.ocorrencias.model.FotoOcorrencia;
import com.humberto.api.ocorrencias.model.Ocorrencia;
import com.humberto.api.ocorrencias.repository.OcorrenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OcorrenciaService {

  private final OcorrenciaRepository ocorrenciaRepository;
  private final ClienteService clienteService;
  private final EnderecoService enderecoService;
  private final LocalStorageService storageService;
  private final FotoOcorrenciaService fotoOcorrenciaService;

  @Transactional
  public OcorrenciaResponse cadastrar(OcorrenciaRequest request, List<MultipartFile> fotos) {
    Cliente cliente = clienteService.buscarOuCriarPorCpf(request.getCliente());
    Endereco endereco = enderecoService.criarEntidade(request.getEndereco());

    Ocorrencia ocorrencia = new Ocorrencia();
    ocorrencia.setCliente(cliente);
    ocorrencia.setEndereco(endereco);

    ocorrencia = ocorrenciaRepository.save(ocorrencia);

    if (fotos != null && !fotos.isEmpty()) {
      fotoOcorrenciaService.adicionarFotos(ocorrencia.getCodOcorrencia(), fotos);
    }

    return toResponse(ocorrenciaRepository.findById(ocorrencia.getCodOcorrencia()).orElseThrow());
  }

  @Transactional(readOnly = true)
  public Page<OcorrenciaResponse> listar(
    String nmeCliente,
    String nroCpf,
    LocalDate dtaOcorrencia,
    String nmeCidade,
    Pageable pageable) {

    String clienteFiltro  = normalizar(nmeCliente);
    String cpfFiltro      = nroCpf != null ? nroCpf.replaceAll("[^0-9]", "") : null;
    String cidadeFiltro   = normalizar(nmeCidade);

    Sort sortTraduzido = Sort.by(
      pageable.getSort().stream()
        .map(order -> {
          String prop = "nmeCidade".equals(order.getProperty())
            ? "endereco.nmeCidade"
            : order.getProperty();
          return order.isAscending() ? Sort.Order.asc(prop) : Sort.Order.desc(prop);
        })
        .toList()
    );
    Pageable pageableTraduzido = PageRequest.of(
      pageable.getPageNumber(), pageable.getPageSize(), sortTraduzido
    );

    return ocorrenciaRepository
      .buscarComFiltros(clienteFiltro, cpfFiltro, dtaOcorrencia, cidadeFiltro, pageableTraduzido)
      .map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public OcorrenciaResponse buscarPorId(Long id) {
    return toResponse(buscarEntidade(id));
  }

  @Transactional
  public OcorrenciaResponse finalizar(Long id) {
    Ocorrencia ocorrencia = buscarEntidade(id);

    if (ocorrencia.isFinalizada()) {
      throw new BusinessException("Ocorrência já está finalizada e não pode ser alterada");
    }

    ocorrencia.finalizar();
    return toResponse(ocorrenciaRepository.save(ocorrencia));
  }

  @Transactional
  public void deletar(Long id) {
    Ocorrencia ocorrencia = buscarEntidade(id);
    if (ocorrencia.isFinalizada()) {
      throw new BusinessException("Ocorrência finalizada não pode ser removida");
    }
    ocorrenciaRepository.deleteById(id);
  }

  private Ocorrencia buscarEntidade(Long id) {
    return ocorrenciaRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Ocorrência não encontrada: " + id));
  }

  private OcorrenciaResponse toResponse(Ocorrencia o) {
    List<FotoResponse> fotos =  new ArrayList<>();
    for (FotoOcorrencia f : o.getFotos()) {
      String url = storageService.gerarUrlDownload(f.getDscPathBucket());
      FotoResponse fotoResponse = new FotoResponse();
      fotoResponse.setCodFotoOcorrencia(f.getCodFotoOcorrencia());
      fotoResponse.setUrlAcesso(url);
      fotoResponse.setDtaCriacao(f.getDtaCriacao());

      fotos.add(fotoResponse);
    }

    OcorrenciaResponse ocorrenciaResponse = new OcorrenciaResponse();
    ocorrenciaResponse.setCodOcorrencia(o.getCodOcorrencia());
    ocorrenciaResponse.setCliente(ClienteResponse.from(o.getCliente()));
    ocorrenciaResponse.setEndereco(EnderecoResponse.from(o.getEndereco()));
    ocorrenciaResponse.setDtaOcorrencia(o.getDtaOcorrencia());
    ocorrenciaResponse.setStaOcorrencia(o.getStaOcorrencia());
    ocorrenciaResponse.setFotos(fotos);

    return ocorrenciaResponse;
  }

  private String normalizar(String valor) {
    return (valor == null || valor.isBlank()) ? null : valor.toLowerCase();
  }
}
