package com.humberto.api.ocorrencias.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "endereco")
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_endereco")
    private Long codEndereco;

    @Column(name = "nme_logradouro", nullable = false, length = 150)
    private String nmeLogradouro;

    @Column(name = "nme_bairro", nullable = false, length = 100)
    private String nmeBairro;

    @Column(name = "nro_cep", nullable = false, length = 8)
    private String nroCep;

    @Column(name = "nme_cidade", nullable = false, length = 100)
    private String nmeCidade;

    @Column(name = "nme_estado", nullable = false, length = 2, columnDefinition = "VARCHAR(2)")
    private String nmeEstado;

    @OneToMany(mappedBy = "endereco", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ocorrencia> ocorrencias = new ArrayList<>();
}
