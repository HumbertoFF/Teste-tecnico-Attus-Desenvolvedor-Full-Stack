package com.humberto.api.ocorrencias.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("api/v1/arquivos")
@Tag(name = "Arquivos", description = "Download de arquivos de ocorrências")
public class ArquivoController {

    @Value("${storage.upload-dir}")
    private String uploadDir;

    @GetMapping("/**")
    @Operation(summary = "Baixar arquivo de ocorrência pelo caminho")
    public ResponseEntity<Resource> download(
            @RequestAttribute(name = "javax.servlet.forward.request_uri", required = false)
            String forwardUri,
            HttpServletRequest request) {
        String requestUri =request.getRequestURI();
        String prefix = "/api/v1/arquivos/";
        String subPath = requestUri.substring(requestUri.indexOf(prefix) + prefix.length());

        try {
            Path filePath = Paths.get(uploadDir).resolve(subPath).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = detectarContentType(subPath);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String detectarContentType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".pdf"))  return "application/pdf";
        return "application/octet-stream";
    }
}
