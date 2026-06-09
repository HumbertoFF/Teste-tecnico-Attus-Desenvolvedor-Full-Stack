package com.humberto.api.ocorrencias.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humberto.api.ocorrencias.config.JwtAuthFilter;
import com.humberto.api.ocorrencias.config.JwtService;
import com.humberto.api.ocorrencias.dto.request.ClienteRequest;
import com.humberto.api.ocorrencias.dto.response.ClienteResponse;
import com.humberto.api.ocorrencias.exception.BusinessException;
import com.humberto.api.ocorrencias.exception.ResourceNotFoundException;
import com.humberto.api.ocorrencias.service.ClienteService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = ClienteController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
)
@DisplayName("ClienteController — testes de controller")
class ClienteControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean ClienteService clienteService;
    @MockitoBean JwtService jwtService;

    private ClienteResponse clienteResponse;
    private ClienteRequest clienteRequest;

    @BeforeEach
    void setUp() {
        clienteResponse = new ClienteResponse();
        clienteResponse.setCodCliente(1L);
        clienteResponse.setNmeCliente("Maria Santos");
        clienteResponse.setNroCpf("98765432100");
        clienteResponse.setDtaNascimento(LocalDate.of(1985, 8, 22));
        clienteResponse.setDtaCriacao(LocalDateTime.now());

        clienteRequest = new ClienteRequest();
        clienteRequest.setNmeCliente("Maria Santos");
        clienteRequest.setNroCpf("987.654.321-00");
        clienteRequest.setDtaNascimento(LocalDate.of(1985, 8, 22));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /clientes: deve retornar 200 com página de clientes")
    void deveListarClientes() throws Exception {
        when(clienteService.listar(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(clienteResponse)));

        mockMvc.perform(get("/api/v1/clientes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].nmeCliente").value("Maria Santos"))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /clientes/{id}: deve retornar 200 para cliente existente")
    void deveBuscarClientePorId() throws Exception {
        when(clienteService.buscarPorId(1L)).thenReturn(clienteResponse);

        mockMvc.perform(get("/api/v1/clientes/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codCliente").value(1))
            .andExpect(jsonPath("$.nroCpf").value("98765432100"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /clientes/{id}: deve retornar 404 para cliente inexistente")
    void deveRetornar404ParaClienteInexistente() throws Exception {
        when(clienteService.buscarPorId(99L))
            .thenThrow(new ResourceNotFoundException("Cliente não encontrado: 99"));

        mockMvc.perform(get("/api/v1/clientes/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /clientes: deve retornar 201 para cliente válido")
    void deveCriarCliente() throws Exception {
        when(clienteService.criar(any())).thenReturn(clienteResponse);

        mockMvc.perform(post("/api/v1/clientes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.codCliente").value(1))
            .andExpect(jsonPath("$.nmeCliente").value("Maria Santos"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /clientes: deve retornar 400 para nome em branco")
    void deveRetornar400ParaNomeEmBranco() throws Exception {
        clienteRequest.setNmeCliente("");

        mockMvc.perform(post("/api/v1/clientes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /clientes: deve retornar 422 para CPF duplicado")
    void deveRetornar422ParaCpfDuplicado() throws Exception {
        when(clienteService.criar(any()))
            .thenThrow(new BusinessException("CPF já cadastrado"));

        mockMvc.perform(post("/api/v1/clientes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteRequest)))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /clientes/{id}: deve retornar 200 para atualização válida")
    void deveAtualizarCliente() throws Exception {
        when(clienteService.atualizar(eq(1L), any())).thenReturn(clienteResponse);

        mockMvc.perform(put("/api/v1/clientes/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nmeCliente").value("Maria Santos"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /clientes/{id}: deve retornar 204 para cliente existente")
    void deveDeletarCliente() throws Exception {
        mockMvc.perform(delete("/api/v1/clientes/1").with(csrf()))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /clientes: deve retornar 401 sem autenticação")
    void deveRetornar401SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/clientes"))
            .andExpect(status().isUnauthorized());
    }
}
