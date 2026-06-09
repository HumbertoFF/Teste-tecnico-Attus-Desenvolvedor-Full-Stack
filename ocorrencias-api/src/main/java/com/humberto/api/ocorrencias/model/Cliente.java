package com.humberto.api.ocorrencias.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_cliente")
    private Long codCliente;

    @Column(name = "nme_cliente", nullable = false, length = 100)
    private String nmeCliente;

    @Column(name = "dta_nascimento", nullable = false)
    private LocalDate dtaNascimento;

    @Column(name = "nro_cpf", nullable = false, unique = true, length = 11)
    private String nroCpf;

    @CreationTimestamp
    @Column(name = "dta_criacao", nullable = false, updatable = false)
    private LocalDateTime dtaCriacao;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ocorrencia> ocorrencias = new ArrayList<Ocorrencia>();
}
