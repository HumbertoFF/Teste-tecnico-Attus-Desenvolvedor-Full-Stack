package com.humberto.api.ocorrencias.service;

import com.humberto.api.ocorrencias.dto.response.FotoResponse;
import com.humberto.api.ocorrencias.exception.BusinessException;
import com.humberto.api.ocorrencias.exception.ResourceNotFoundException;
import com.humberto.api.ocorrencias.model.FotoOcorrencia;
import com.humberto.api.ocorrencias.model.Ocorrencia;
import com.humberto.api.ocorrencias.repository.FotoOcorrenciaRepository;
import com.humberto.api.ocorrencias.repository.OcorrenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FotoOcorrenciaService {

  private final OcorrenciaRepository ocorrenciaRepository;
  private final FotoOcorrenciaRepository fotoOcorrenciaRepository;
  private final LocalStorageService storageService;

  @Transactional
  public List<FotoResponse> adicionarFotos(Long codOcorrencia, List<MultipartFile> arquivos) {
    Ocorrencia ocorrencia = ocorrenciaRepository.findById(codOcorrencia)
      .orElseThrow(() -> new ResourceNotFoundException("Ocorrência não encontrada: " + codOcorrencia));

    if (ocorrencia.isFinalizada()) {
      throw new BusinessException("Não é possível adicionar fotos a uma ocorrência finalizada");
    }

    for (MultipartFile arquivo : arquivos) {
      if (arquivo == null || arquivo.isEmpty()) continue;

      String prefixo = "ocorrencias/" + codOcorrencia;
      LocalStorageService.UploadResult resultado = storageService.upload(arquivo, prefixo);

      FotoOcorrencia foto = new FotoOcorrencia();
      foto.setOcorrencia(ocorrencia);
      foto.setDscPathBucket(resultado.pathBucket());
      foto.setDscHash(resultado.hash());

      fotoOcorrenciaRepository.save(foto);
    }

    return fotoOcorrenciaRepository
      .findByOcorrencia_CodOcorrencia(codOcorrencia)
      .stream()
      .map(this::toResponse)
      .toList();
  }

  private FotoResponse toResponse(FotoOcorrencia foto) {
    FotoResponse response = new FotoResponse();
    response.setCodFotoOcorrencia(foto.getCodFotoOcorrencia());
    response.setUrlAcesso(storageService.gerarUrlDownload(foto.getDscPathBucket()));
    response.setDtaCriacao(foto.getDtaCriacao());
    return response;
  }
}
