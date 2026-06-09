package com.humberto.api.ocorrencias.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_usuario")
    private Long codUsuario;

    @Column(name = "nme_usuario", nullable = false, length = 100)
    private String nmeUsuario;

    @Column(name = "dsc_email", nullable = false, unique = true, length = 150)
    private String dscEmail;

    @Column(name = "dsc_senha", nullable = false, length = 255)
    private String dscSenha;

    @CreationTimestamp
    @Column(name = "dta_criacao", nullable = false, updatable = false)
    private LocalDateTime dtaCriacao;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return dscSenha;
    }

    @Override
    public String getUsername() {
        return dscEmail;
    }
}
