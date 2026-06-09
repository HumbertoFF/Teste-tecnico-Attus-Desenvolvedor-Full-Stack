package com.humberto.api.ocorrencias.model;

import com.humberto.api.ocorrencias.model.enums.StatusOcorrencia;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ocorrencia")
public class Ocorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_ocorrencia")
    private Long codOcorrencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_endereco", nullable = false)
    private Endereco endereco;

    @CreationTimestamp
    @Column(name = "dta_ocorrencia", nullable = false, updatable = false)
    private LocalDate dtaOcorrencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "sta_ocorrencia", nullable = false, length = 20)
    private StatusOcorrencia staOcorrencia = StatusOcorrencia.ATIVA;

    @OneToMany(mappedBy = "ocorrencia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FotoOcorrencia> fotos = new ArrayList<>();

    public boolean isFinalizada() {
      return StatusOcorrencia.FINALIZADA.equals(this.staOcorrencia);
    }

    public void finalizar() {
      this.staOcorrencia = StatusOcorrencia.FINALIZADA;
    }
}
