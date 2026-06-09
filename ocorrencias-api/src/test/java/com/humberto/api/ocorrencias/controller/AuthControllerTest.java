package com.humberto.api.ocorrencias.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humberto.api.ocorrencias.config.JwtAuthFilter;
import com.humberto.api.ocorrencias.config.JwtService;
import com.humberto.api.ocorrencias.dto.request.LoginRequest;
import com.humberto.api.ocorrencias.dto.response.LoginResponse;
import com.humberto.api.ocorrencias.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AuthController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
)
@DisplayName("AuthController — testes de controller")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AuthService authService;
    @MockitoBean JwtService jwtService;

    @Test
    @DisplayName("POST /auth/login: deve retornar 200 e token para credenciais válidas")
    void deveRetornar200ComToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@admin.com.br");
        request.setSenha("admin123");

        LoginResponse response = new LoginResponse();
        response.setToken("fake-jwt-token");
        response.setTipo("Bearer");
        response.setExpiracaoMs(1800000L);

        when(authService.autenticar(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("fake-jwt-token"))
            .andExpect(jsonPath("$.tipo").value("Bearer"))
            .andExpect(jsonPath("$.expiracaoMs").value(1800000));
    }

    @Test
    @DisplayName("POST /auth/login: deve retornar 400 para email em branco")
    void deveRetornar400ParaEmailEmBranco() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setSenha("admin123");

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login: deve retornar 400 para senha em branco")
    void deveRetornar400ParaSenhaEmBranco() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@admin.com.br");
        request.setSenha("");

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login: deve retornar 401 para credenciais inválidas")
    void deveRetornar401ParaCredenciaisInvalidas() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@admin.com.br");
        request.setSenha("senha-errada");

        when(authService.autenticar(any())).thenThrow(new BadCredentialsException("Email ou senha inválidos"));

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login: deve retornar 401 para email não cadastrado")
    void deveRetornar401ParaEmailNaoCadastrado() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("naoexiste@email.com");
        request.setSenha("qualquer");

        when(authService.autenticar(any())).thenThrow(new UsernameNotFoundException("Email ou senha inválidos"));

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
}
