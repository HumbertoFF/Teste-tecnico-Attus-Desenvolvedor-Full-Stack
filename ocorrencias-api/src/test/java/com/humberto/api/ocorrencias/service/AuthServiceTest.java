package com.humberto.api.ocorrencias.service;

import com.humberto.api.ocorrencias.config.JwtService;
import com.humberto.api.ocorrencias.dto.request.LoginRequest;
import com.humberto.api.ocorrencias.dto.response.LoginResponse;
import com.humberto.api.ocorrencias.model.Usuario;
import com.humberto.api.ocorrencias.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — testes unitários")
class AuthServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock JwtService jwtService;

    @InjectMocks AuthService authService;

    private Usuario usuario;
    private LoginRequest loginRequest;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "expiracaoMs", 1800000L);

        usuario = new Usuario();
        usuario.setDscEmail("admin@admin.com.br");
        usuario.setDscSenha(encoder.encode("admin123"));

        loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@admin.com.br");
        loginRequest.setSenha("admin123");
    }

    @Test
    @DisplayName("autenticar: deve retornar token JWT para credenciais válidas")
    void deveAutenticarComSucesso() {
        when(usuarioRepository.findByDscEmail("admin@admin.com.br"))
            .thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(usuario)).thenReturn("fake-jwt-token");

        LoginResponse response = authService.autenticar(loginRequest);

        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getTipo()).isEqualTo("Bearer");
        assertThat(response.getExpiracaoMs()).isEqualTo(1800000L);
        verify(jwtService).generateToken(usuario);
    }

    @Test
    @DisplayName("autenticar: deve lançar UsernameNotFoundException para email inexistente")
    void deveLancarExcecaoParaEmailInexistente() {
        when(usuarioRepository.findByDscEmail(any())).thenReturn(Optional.empty());
        loginRequest.setEmail("naoexiste@email.com");

        assertThatThrownBy(() -> authService.autenticar(loginRequest))
                .isInstanceOf(UsernameNotFoundException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("autenticar: deve lançar BadCredentialsException para senha incorreta")
    void deveLancarExcecaoParaSenhaIncorreta() {
        when(usuarioRepository.findByDscEmail("admin@admin.com.br"))
            .thenReturn(Optional.of(usuario));
        loginRequest.setSenha("senha-errada");

        assertThatThrownBy(() -> authService.autenticar(loginRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("autenticar: não deve expor se o erro é no email ou na senha")
    void deveMostrarMesmaMensagemParaEmailESenhaInvalidos() {
        when(usuarioRepository.findByDscEmail(any())).thenReturn(Optional.empty());
        loginRequest.setEmail("qualquer@email.com");
        Throwable erroEmail = catchThrowable(() -> authService.autenticar(loginRequest));

        when(usuarioRepository.findByDscEmail("admin@admin.com.br"))
            .thenReturn(Optional.of(usuario));
        loginRequest.setEmail("admin@admin.com.br");
        loginRequest.setSenha("errada");
        Throwable erroSenha = catchThrowable(() -> authService.autenticar(loginRequest));

        assertThat(erroEmail.getMessage()).isEqualTo(erroSenha.getMessage());
    }
}
