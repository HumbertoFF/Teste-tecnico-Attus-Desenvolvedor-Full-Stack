package com.humberto.api.ocorrencias.service;

import com.humberto.api.ocorrencias.dto.response.FotoResponse;
import com.humberto.api.ocorrencias.exception.BusinessException;
import com.humberto.api.ocorrencias.exception.ResourceNotFoundException;
import com.humberto.api.ocorrencias.model.FotoOcorrencia;
import com.humberto.api.ocorrencias.model.Ocorrencia;
import com.humberto.api.ocorrencias.model.enums.StatusOcorrencia;
import com.humberto.api.ocorrencias.repository.FotoOcorrenciaRepository;
import com.humberto.api.ocorrencias.repository.OcorrenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FotoOcorrenciaService — testes unitários")
class FotoOcorrenciaServiceTest {

    @Mock OcorrenciaRepository ocorrenciaRepository;
    @Mock FotoOcorrenciaRepository fotoOcorrenciaRepository;
    @Mock LocalStorageService storageService;

    @InjectMocks FotoOcorrenciaService fotoOcorrenciaService;

    private Ocorrencia ocorrenciaAtiva;
    private Ocorrencia ocorrenciaFinalizada;
    private FotoOcorrencia fotoSalva;

    @BeforeEach
    void setUp() {
        ocorrenciaAtiva = new Ocorrencia();
        ocorrenciaAtiva.setCodOcorrencia(1L);
        ocorrenciaAtiva.setStaOcorrencia(StatusOcorrencia.ATIVA);
        ocorrenciaAtiva.setDtaOcorrencia(LocalDate.now());
        ocorrenciaAtiva.setFotos(new ArrayList<>());

        ocorrenciaFinalizada = new Ocorrencia();
        ocorrenciaFinalizada.setCodOcorrencia(2L);
        ocorrenciaFinalizada.setStaOcorrencia(StatusOcorrencia.FINALIZADA);
        ocorrenciaFinalizada.setDtaOcorrencia(LocalDate.now());
        ocorrenciaFinalizada.setFotos(new ArrayList<>());

        fotoSalva = new FotoOcorrencia();
        fotoSalva.setCodFotoOcorrencia(1L);
        fotoSalva.setOcorrencia(ocorrenciaAtiva);
        fotoSalva.setDscPathBucket("ocorrencias/1/uuid.jpg");
        fotoSalva.setDscHash("abc123");
        fotoSalva.setDtaCriacao(LocalDateTime.now());
    }

    @Test
    @DisplayName("adicionarFotos: deve salvar arquivo e retornar lista")
    void deveAdicionarFotos() {
        MockMultipartFile arquivo = new MockMultipartFile(
            "arquivos", "foto.jpg", "image/jpeg", "conteudo".getBytes()
        );

        when(ocorrenciaRepository.findById(1L)).thenReturn(Optional.of(ocorrenciaAtiva));
        when(storageService.upload(any(), eq("ocorrencias/1")))
            .thenReturn(new LocalStorageService.UploadResult("ocorrencias/1/uuid.jpg", "abc123"));
        when(fotoOcorrenciaRepository.findByOcorrencia_CodOcorrencia(1L))
            .thenReturn(List.of(fotoSalva));
        when(storageService.gerarUrlDownload("ocorrencias/1/uuid.jpg"))
            .thenReturn("http://localhost:8080/api/v1/arquivos/ocorrencias/1/uuid.jpg");

        List<FotoResponse> result = fotoOcorrenciaService.adicionarFotos(1L, List.of(arquivo));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUrlAcesso()).contains("uuid.jpg");
        verify(fotoOcorrenciaRepository).save(any(FotoOcorrencia.class));
        verify(storageService).upload(any(), eq("ocorrencias/1"));
    }

    @Test
    @DisplayName("adicionarFotos: deve lançar BusinessException para ocorrência FINALIZADA")
    void deveLancarExcecaoParaOcorrenciaFinalizada() {
        MockMultipartFile arquivo = new MockMultipartFile(
            "arquivos", "foto.jpg", "image/jpeg", "conteudo".getBytes()
        );

        when(ocorrenciaRepository.findById(2L)).thenReturn(Optional.of(ocorrenciaFinalizada));

        assertThatThrownBy(() -> fotoOcorrenciaService.adicionarFotos(2L, List.of(arquivo)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("finalizada");

        verify(storageService, never()).upload(any(), any());
        verify(fotoOcorrenciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("adicionarFotos: deve lançar ResourceNotFoundException para ocorrência inexistente")
    void deveLancarExcecaoParaOcorrenciaInexistente() {
        MockMultipartFile arquivo = new MockMultipartFile(
            "arquivos", "foto.jpg", "image/jpeg", "conteudo".getBytes()
        );

        when(ocorrenciaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fotoOcorrenciaService.adicionarFotos(99L, List.of(arquivo)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("adicionarFotos: deve ignorar arquivos vazios")
    void deveIgnorarArquivosVazios() {
        MockMultipartFile arquivoVazio = new MockMultipartFile(
            "arquivos", "vazio.jpg", "image/jpeg", new byte[0]
        );

        when(ocorrenciaRepository.findById(1L)).thenReturn(Optional.of(ocorrenciaAtiva));
        when(fotoOcorrenciaRepository.findByOcorrencia_CodOcorrencia(1L))
            .thenReturn(List.of());

        List<FotoResponse> result = fotoOcorrenciaService.adicionarFotos(1L, List.of(arquivoVazio));

        assertThat(result).isEmpty();
        verify(storageService, never()).upload(any(), any());
        verify(fotoOcorrenciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("adicionarFotos: deve salvar múltiplos arquivos")
    void deveSalvarMultiplosArquivos() {
        MockMultipartFile arquivo1 = new MockMultipartFile(
            "arquivos", "foto1.jpg", "image/jpeg", "img1".getBytes()
        );
        MockMultipartFile arquivo2 = new MockMultipartFile(
            "arquivos", "doc.pdf", "application/pdf", "pdf".getBytes()
        );

        when(ocorrenciaRepository.findById(1L)).thenReturn(Optional.of(ocorrenciaAtiva));
        when(storageService.upload(any(), eq("ocorrencias/1")))
            .thenReturn(new LocalStorageService.UploadResult("ocorrencias/1/uuid1.jpg", "hash1"))
            .thenReturn(new LocalStorageService.UploadResult("ocorrencias/1/uuid2.pdf", "hash2"));
        when(fotoOcorrenciaRepository.findByOcorrencia_CodOcorrencia(1L))
            .thenReturn(List.of(fotoSalva));
        when(storageService.gerarUrlDownload(any()))
            .thenReturn("http://localhost:8080/api/v1/arquivos/x");

        fotoOcorrenciaService.adicionarFotos(1L, List.of(arquivo1, arquivo2));

        verify(fotoOcorrenciaRepository, times(2)).save(any(FotoOcorrencia.class));
        verify(storageService, times(2)).upload(any(), eq("ocorrencias/1"));
    }
}
