package com.humberto.api.ocorrencias.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

@DisplayName("LocalStorageService — testes unitários")
class LocalStorageServiceTest {

    private LocalStorageService storageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        storageService = new LocalStorageService();
        ReflectionTestUtils.setField(storageService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(storageService, "baseUrl", "http://localhost:8080");
    }

    @Test
    @DisplayName("upload: deve salvar arquivo e retornar path relativo e hash SHA-256")
    void deveFazerUploadComSucesso() {
        MockMultipartFile arquivo = new MockMultipartFile(
            "arquivos", "foto.jpg", "image/jpeg", "conteudo-da-imagem".getBytes()
        );

        LocalStorageService.UploadResult resultado = storageService.upload(arquivo, "ocorrencias/1");

        assertThat(resultado.pathBucket()).startsWith("ocorrencias/1/");
        assertThat(resultado.pathBucket()).endsWith(".jpg");
        assertThat(resultado.hash()).hasSize(64); // SHA-256 em hex
    }

    @Test
    @DisplayName("upload: deve criar subdiretórios automaticamente")
    void deveCriarSubdiretorios() {
        MockMultipartFile arquivo = new MockMultipartFile(
            "arquivos", "doc.pdf", "application/pdf", "pdf-content".getBytes()
        );

        storageService.upload(arquivo, "ocorrencias/42");

        assertThat(tempDir.resolve("ocorrencias/42").toFile()).isDirectory();
    }

    @Test
    @DisplayName("upload: deve preservar a extensão do arquivo original")
    void devePreservarExtensao() {
        MockMultipartFile png = new MockMultipartFile(
            "arquivos", "imagem.png", "image/png", "png".getBytes()
        );
        MockMultipartFile pdf = new MockMultipartFile(
            "arquivos", "doc.pdf", "application/pdf", "pdf".getBytes()
        );

        assertThat(storageService.upload(png, "ocorrencias/1").pathBucket()).endsWith(".png");
        assertThat(storageService.upload(pdf, "ocorrencias/1").pathBucket()).endsWith(".pdf");
    }

    @Test
    @DisplayName("upload: deve gerar nomes únicos para arquivos com mesmo nome")
    void deveGerarNomesUnicos() {
        MockMultipartFile a1 = new MockMultipartFile("arquivos", "foto.jpg", "image/jpeg", "img1".getBytes());
        MockMultipartFile a2 = new MockMultipartFile("arquivos", "foto.jpg", "image/jpeg", "img2".getBytes());

        LocalStorageService.UploadResult r1 = storageService.upload(a1, "ocorrencias/1");
        LocalStorageService.UploadResult r2 = storageService.upload(a2, "ocorrencias/1");

        assertThat(r1.pathBucket()).isNotEqualTo(r2.pathBucket());
    }

    @Test
    @DisplayName("upload: deve gerar hashes diferentes para conteúdos diferentes")
    void deveGerarHashesDiferentes() {
        MockMultipartFile a1 = new MockMultipartFile("arquivos", "a.jpg", "image/jpeg", "conteudo-A".getBytes());
        MockMultipartFile a2 = new MockMultipartFile("arquivos", "b.jpg", "image/jpeg", "conteudo-B".getBytes());

        LocalStorageService.UploadResult r1 = storageService.upload(a1, "ocorrencias/1");
        LocalStorageService.UploadResult r2 = storageService.upload(a2, "ocorrencias/1");

        assertThat(r1.hash()).isNotEqualTo(r2.hash());
    }

    @Test
    @DisplayName("gerarUrlDownload: deve montar URL correta")
    void deveGerarUrlDownload() {
        String url = storageService.gerarUrlDownload("ocorrencias/1/uuid.jpg");

        assertThat(url).isEqualTo("http://localhost:8080/api/v1/arquivos/ocorrencias/1/uuid.jpg");
    }
}
