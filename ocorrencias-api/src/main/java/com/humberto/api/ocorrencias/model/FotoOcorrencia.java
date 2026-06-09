package com.humberto.api.ocorrencias.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "foto_ocorrencia")
public class FotoOcorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_foto_ocorrencia")
    private Long codFotoOcorrencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_ocorrencia", nullable = false)
    private Ocorrencia ocorrencia;

    @CreationTimestamp
    @Column(name = "dta_criacao", nullable = false, updatable = false)
    private LocalDateTime dtaCriacao;

    @Column(name = "dsc_path_bucket", nullable = false, length = 500)
    private String dscPathBucket;

    @Column(name = "dsc_hash", nullable = false, length = 64)
    private String dscHash;
}
