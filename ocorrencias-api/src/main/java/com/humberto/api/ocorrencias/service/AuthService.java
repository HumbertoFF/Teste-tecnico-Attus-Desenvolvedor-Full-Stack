package com.humberto.api.ocorrencias.service;

import com.humberto.api.ocorrencias.config.JwtService;
import com.humberto.api.ocorrencias.dto.request.LoginRequest;
import com.humberto.api.ocorrencias.dto.response.LoginResponse;
import com.humberto.api.ocorrencias.model.Usuario;
import com.humberto.api.ocorrencias.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
  private static final Logger log = LoggerFactory.getLogger(AuthService.class);
  private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();
  private final UsuarioRepository usuarioRepository;
  private final JwtService jwtService;

  @Value("${security.jwt.expiration-ms}")
  private long expiracaoMs;

  public LoginResponse autenticar(LoginRequest request) {

    Usuario usuario = usuarioRepository.findByDscEmail(request.getEmail())
      .orElseThrow(() -> {
        log.warn("Email não encontrado: {}", request.getEmail());
        return new UsernameNotFoundException("Email ou senha inválidos");
      });

    String hashNoBanco = usuario.getPassword();
    String senhaDigitada = request.getSenha();

    boolean matches = ENCODER.matches(senhaDigitada, hashNoBanco);

    if (!matches) {
      log.warn("Senha incorreta para: {}", request.getEmail());
      throw new BadCredentialsException("Email ou senha inválidos");
    }

    String token = jwtService.generateToken(usuario);
    LoginResponse loginResponse = new LoginResponse();
    loginResponse.setToken(token);
    loginResponse.setTipo("Bearer");
    loginResponse.setExpiracaoMs(expiracaoMs);

    return loginResponse;
  }
}
