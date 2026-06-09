package com.humberto.api.ocorrencias.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humberto.api.ocorrencias.config.JwtAuthFilter;
import com.humberto.api.ocorrencias.config.JwtService;
import com.humberto.api.ocorrencias.dto.request.ClienteRequest;
import com.humberto.api.ocorrencias.dto.request.EnderecoRequest;
import com.humberto.api.ocorrencias.dto.request.OcorrenciaRequest;
import com.humberto.api.ocorrencias.dto.response.ClienteResponse;
import com.humberto.api.ocorrencias.dto.response.EnderecoResponse;
import com.humberto.api.ocorrencias.model.Endereco;
import com.humberto.api.ocorrencias.dto.response.FotoResponse;
import com.humberto.api.ocorrencias.dto.response.OcorrenciaResponse;
import com.humberto.api.ocorrencias.exception.BusinessException;
import com.humberto.api.ocorrencias.exception.ResourceNotFoundException;
import com.humberto.api.ocorrencias.model.enums.StatusOcorrencia;
import com.humberto.api.ocorrencias.service.FotoOcorrenciaService;
import com.humberto.api.ocorrencias.service.OcorrenciaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = OcorrenciaController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
)
@DisplayName("OcorrenciaController — testes de controller")
class OcorrenciaControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean OcorrenciaService ocorrenciaService;
    @MockitoBean FotoOcorrenciaService fotoOcorrenciaService;
    @MockitoBean JwtService jwtService;

    private OcorrenciaResponse ocorrenciaAtiva;
    private OcorrenciaResponse ocorrenciaFinalizada;

    @BeforeEach
    void setUp() {
        ClienteResponse cliente = new ClienteResponse();
        cliente.setCodCliente(1L);
        cliente.setNmeCliente("Maria Santos");
        cliente.setNroCpf("98765432100");
        cliente.setDtaNascimento(LocalDate.of(1985, 8, 22));
        cliente.setDtaCriacao(LocalDateTime.now());

        Endereco enderecoEntidade = new Endereco();
        enderecoEntidade.setCodEndereco(1L);
        enderecoEntidade.setNmeLogradouro("Rua XV de Novembro, 200");
        enderecoEntidade.setNmeBairro("Centro");
        enderecoEntidade.setNroCep("80020310");
        enderecoEntidade.setNmeCidade("Curitiba");
        enderecoEntidade.setNmeEstado("PR");
        EnderecoResponse endereco = EnderecoResponse.from(enderecoEntidade);

        ocorrenciaAtiva = new OcorrenciaResponse();
        ocorrenciaAtiva.setCodOcorrencia(1L);
        ocorrenciaAtiva.setCliente(cliente);
        ocorrenciaAtiva.setEndereco(endereco);
        ocorrenciaAtiva.setDtaOcorrencia(LocalDate.now());
        ocorrenciaAtiva.setStaOcorrencia(StatusOcorrencia.ATIVA);
        ocorrenciaAtiva.setFotos(new ArrayList<>());

        ocorrenciaFinalizada = new OcorrenciaResponse();
        ocorrenciaFinalizada.setCodOcorrencia(2L);
        ocorrenciaFinalizada.setCliente(cliente);
        ocorrenciaFinalizada.setEndereco(endereco);
        ocorrenciaFinalizada.setDtaOcorrencia(LocalDate.now());
        ocorrenciaFinalizada.setStaOcorrencia(StatusOcorrencia.FINALIZADA);
        ocorrenciaFinalizada.setFotos(new ArrayList<>());
    }

    // ─── POST /ocorrencias ────────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("POST /ocorrencias: deve retornar 201 para ocorrência válida")
    void deveCadastrarOcorrencia() throws Exception {
        when(ocorrenciaService.cadastrar(any(), any())).thenReturn(ocorrenciaAtiva);

        MockMultipartFile dados = new MockMultipartFile(
            "dados", "", MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(buildRequest())
        );

        mockMvc.perform(multipart("/api/v1/ocorrencias")
                .file(dados)
                .with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.codOcorrencia").value(1))
            .andExpect(jsonPath("$.staOcorrencia").value("ATIVA"))
            .andExpect(jsonPath("$.cliente.nmeCliente").value("Maria Santos"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /ocorrencias: deve retornar 400 para dados inválidos")
    void deveRetornar400ParaDadosInvalidos() throws Exception {
        OcorrenciaRequest requestInvalida = new OcorrenciaRequest();
        // sem cliente e sem endereço

        MockMultipartFile dados = new MockMultipartFile(
            "dados", "", MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(requestInvalida)
        );

        mockMvc.perform(multipart("/api/v1/ocorrencias")
                .file(dados)
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }

    // ─── GET /ocorrencias ─────────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /ocorrencias: deve retornar 200 com página de ocorrências")
    void deveListarOcorrencias() throws Exception {
        when(ocorrenciaService.listar(any(), any(), any(), any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(ocorrenciaAtiva)));

        mockMvc.perform(get("/api/v1/ocorrencias"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].codOcorrencia").value(1))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /ocorrencias: deve aceitar filtros por nome e cidade")
    void deveListarComFiltros() throws Exception {
        when(ocorrenciaService.listar(eq("Maria"), any(), any(), eq("Curitiba"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(ocorrenciaAtiva)));

        mockMvc.perform(get("/api/v1/ocorrencias")
                .param("nmeCliente", "Maria")
                .param("nmeCidade", "Curitiba"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].cliente.nmeCliente").value("Maria Santos"));
    }

    // ─── GET /ocorrencias/{id} ────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /ocorrencias/{id}: deve retornar 200 para ocorrência existente")
    void deveBuscarPorId() throws Exception {
        when(ocorrenciaService.buscarPorId(1L)).thenReturn(ocorrenciaAtiva);

        mockMvc.perform(get("/api/v1/ocorrencias/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codOcorrencia").value(1))
            .andExpect(jsonPath("$.endereco.nmeCidade").value("Curitiba"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /ocorrencias/{id}: deve retornar 404 para ocorrência inexistente")
    void deveRetornar404ParaOcorrenciaInexistente() throws Exception {
        when(ocorrenciaService.buscarPorId(99L))
            .thenThrow(new ResourceNotFoundException("Ocorrência não encontrada: 99"));

        mockMvc.perform(get("/api/v1/ocorrencias/99"))
            .andExpect(status().isNotFound());
    }

    // ─── PATCH /ocorrencias/{id}/finalizar ────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("PATCH /ocorrencias/{id}/finalizar: deve retornar 200 com status FINALIZADA")
    void deveFinalizar() throws Exception {
        when(ocorrenciaService.finalizar(1L)).thenReturn(ocorrenciaFinalizada);

        mockMvc.perform(patch("/api/v1/ocorrencias/1/finalizar").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.staOcorrencia").value("FINALIZADA"));
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /ocorrencias/{id}/finalizar: deve retornar 422 para ocorrência já finalizada")
    void deveRetornar422AoFinalizarJaFinalizada() throws Exception {
        when(ocorrenciaService.finalizar(2L))
            .thenThrow(new BusinessException("Ocorrência já está finalizada"));

        mockMvc.perform(patch("/api/v1/ocorrencias/2/finalizar").with(csrf()))
            .andExpect(status().isUnprocessableEntity());
    }

    // ─── POST /ocorrencias/{id}/fotos ─────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("POST /ocorrencias/{id}/fotos: deve retornar 200 com lista de arquivos")
    void deveAdicionarFotos() throws Exception {
        FotoResponse foto = new FotoResponse();
        foto.setCodFotoOcorrencia(1L);
        foto.setUrlAcesso("http://localhost:8080/api/v1/arquivos/ocorrencias/1/uuid.jpg");
        foto.setDtaCriacao(LocalDateTime.now());

        when(fotoOcorrenciaService.adicionarFotos(eq(1L), any())).thenReturn(List.of(foto));

        MockMultipartFile arquivo = new MockMultipartFile(
            "fotos", "foto.jpg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/ocorrencias/1/fotos")
                .file(arquivo)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].urlAcesso").value("http://localhost:8080/api/v1/arquivos/ocorrencias/1/uuid.jpg"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /ocorrencias/{id}/fotos: deve retornar 422 para ocorrência finalizada")
    void deveRetornar422AoAdicionarFotosEmFinalizada() throws Exception {
        when(fotoOcorrenciaService.adicionarFotos(eq(2L), any()))
            .thenThrow(new BusinessException("Não é possível adicionar fotos a uma ocorrência finalizada"));

        MockMultipartFile arquivo = new MockMultipartFile(
            "fotos", "foto.jpg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/ocorrencias/2/fotos")
                .file(arquivo)
                .with(csrf()))
            .andExpect(status().isUnprocessableEntity());
    }

    // ─── DELETE /ocorrencias/{id} ─────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("DELETE /ocorrencias/{id}: deve retornar 204 para ocorrência ATIVA")
    void deveDeletarOcorrencia() throws Exception {
        mockMvc.perform(delete("/api/v1/ocorrencias/1").with(csrf()))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /ocorrencias/{id}: deve retornar 422 para ocorrência FINALIZADA")
    void deveRetornar422AoDeletarFinalizada() throws Exception {
        when(ocorrenciaService.finalizar(2L))
            .thenThrow(new BusinessException("Ocorrência finalizada não pode ser removida"));

        mockMvc.perform(delete("/api/v1/ocorrencias/2").with(csrf()))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("GET /ocorrencias: deve retornar 401 sem autenticação")
    void deveRetornar401SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/ocorrencias"))
            .andExpect(status().isUnauthorized());
    }

    // ─── helper ───────────────────────────────────────────────────────────────

    private OcorrenciaRequest buildRequest() {
        ClienteRequest clienteReq = new ClienteRequest();
        clienteReq.setNmeCliente("Maria Santos");
        clienteReq.setNroCpf("987.654.321-00");
        clienteReq.setDtaNascimento(LocalDate.of(1985, 8, 22));

        EnderecoRequest enderecoReq = new EnderecoRequest();
        enderecoReq.setNmeLogradouro("Rua XV de Novembro, 200");
        enderecoReq.setNmeBairro("Centro");
        enderecoReq.setNroCep("80020310");
        enderecoReq.setNmeCidade("Curitiba");
        enderecoReq.setNmeEstado("PR");

        OcorrenciaRequest req = new OcorrenciaRequest();
        req.setCliente(clienteReq);
        req.setEndereco(enderecoReq);
        return req;
    }
}
