package com.humberto.api.ocorrencias.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humberto.api.ocorrencias.config.JwtAuthFilter;
import com.humberto.api.ocorrencias.config.JwtService;
import com.humberto.api.ocorrencias.dto.request.EnderecoRequest;
import com.humberto.api.ocorrencias.dto.response.EnderecoResponse;
import com.humberto.api.ocorrencias.model.Endereco;
import com.humberto.api.ocorrencias.exception.ResourceNotFoundException;
import com.humberto.api.ocorrencias.service.EnderecoService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = EnderecoController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
)
@DisplayName("EnderecoController — testes de controller")
class EnderecoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean EnderecoService enderecoService;
    @MockitoBean JwtService jwtService;

    private EnderecoResponse enderecoResponse;
    private EnderecoRequest enderecoRequest;

    @BeforeEach
    void setUp() {
        Endereco enderecoEntidade = new Endereco();
        enderecoEntidade.setCodEndereco(1L);
        enderecoEntidade.setNmeLogradouro("Rua XV de Novembro, 200");
        enderecoEntidade.setNmeBairro("Centro");
        enderecoEntidade.setNroCep("80020310");
        enderecoEntidade.setNmeCidade("Curitiba");
        enderecoEntidade.setNmeEstado("PR");
        enderecoResponse = EnderecoResponse.from(enderecoEntidade);

        enderecoRequest = new EnderecoRequest();
        enderecoRequest.setNmeLogradouro("Rua XV de Novembro, 200");
        enderecoRequest.setNmeBairro("Centro");
        enderecoRequest.setNroCep("80020310");
        enderecoRequest.setNmeCidade("Curitiba");
        enderecoRequest.setNmeEstado("PR");
    }

    @Test
    @WithMockUser
    @DisplayName("GET /enderecos: deve retornar 200 com página de endereços")
    void deveListarEnderecos() throws Exception {
        when(enderecoService.listar(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(enderecoResponse)));

        mockMvc.perform(get("/api/v1/enderecos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].nmeCidade").value("Curitiba"))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /enderecos/{id}: deve retornar 200 para endereço existente")
    void deveBuscarEnderecoPorId() throws Exception {
        when(enderecoService.buscarPorId(1L)).thenReturn(enderecoResponse);

        mockMvc.perform(get("/api/v1/enderecos/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codEndereco").value(1))
            .andExpect(jsonPath("$.nmeEstado").value("PR"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /enderecos/{id}: deve retornar 404 para endereço inexistente")
    void deveRetornar404ParaEnderecoInexistente() throws Exception {
        when(enderecoService.buscarPorId(99L))
            .thenThrow(new ResourceNotFoundException("Endereço não encontrado: 99"));

        mockMvc.perform(get("/api/v1/enderecos/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /enderecos: deve retornar 201 para endereço válido")
    void deveCriarEndereco() throws Exception {
        when(enderecoService.criar(any())).thenReturn(enderecoResponse);

        mockMvc.perform(post("/api/v1/enderecos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enderecoRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nmeCidade").value("Curitiba"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /enderecos: deve retornar 400 para logradouro em branco")
    void deveRetornar400ParaLogradouroEmBranco() throws Exception {
        enderecoRequest.setNmeLogradouro("");

        mockMvc.perform(post("/api/v1/enderecos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enderecoRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /enderecos: deve retornar 400 para CEP com tamanho inválido")
    void deveRetornar400ParaCepInvalido() throws Exception {
        enderecoRequest.setNroCep("123");

        mockMvc.perform(post("/api/v1/enderecos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enderecoRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /enderecos/{id}: deve retornar 200 para atualização válida")
    void deveAtualizarEndereco() throws Exception {
        when(enderecoService.atualizar(eq(1L), any())).thenReturn(enderecoResponse);

        mockMvc.perform(put("/api/v1/enderecos/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enderecoRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nroCep").value("80020310"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /enderecos/{id}: deve retornar 204 para endereço existente")
    void deveDeletarEndereco() throws Exception {
        mockMvc.perform(delete("/api/v1/enderecos/1").with(csrf()))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /enderecos: deve retornar 401 sem autenticação")
    void deveRetornar401SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/enderecos"))
            .andExpect(status().isUnauthorized());
    }
}
