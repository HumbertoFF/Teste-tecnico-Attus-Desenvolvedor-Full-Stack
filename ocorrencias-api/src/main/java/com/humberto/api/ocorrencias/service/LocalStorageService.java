package com.humberto.api.ocorrencias.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class LocalStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    @Value("${storage.upload-dir}")
    private String uploadDir;

    @Value("${storage.base-url}")
    private String baseUrl;

    public record UploadResult(String pathBucket, String hash) {}

    public UploadResult upload(MultipartFile arquivo, String prefixo) {
        try {
            Path destDir = Paths.get(uploadDir, prefixo);
            Files.createDirectories(destDir);

            String extensao = obterExtensao(arquivo.getOriginalFilename());
            String nomeArquivo = UUID.randomUUID() + extensao;
            Path destPath = destDir.resolve(nomeArquivo);

            arquivo.transferTo(destPath);

            String relativePath = prefixo + "/" + nomeArquivo;
            String hash = calcularHash(arquivo.getBytes());

            log.info("Arquivo salvo: {}", destPath);
            return new UploadResult(relativePath, hash);

        } catch (IOException | java.security.NoSuchAlgorithmException e) {
            log.error("Erro ao salvar arquivo", e);
            throw new RuntimeException("Falha ao armazenar arquivo: " + e.getMessage(), e);
        }
    }

    public String gerarUrlDownload(String relativePath) {
        return baseUrl + "/api/v1/arquivos/" + relativePath;
    }

    private String obterExtensao(String nomeArquivo) {
        if (nomeArquivo != null && nomeArquivo.contains(".")) {
            return nomeArquivo.substring(nomeArquivo.lastIndexOf("."));
        }
        return "";
    }

    private String calcularHash(byte[] bytes) throws java.security.NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(bytes));
    }
}
