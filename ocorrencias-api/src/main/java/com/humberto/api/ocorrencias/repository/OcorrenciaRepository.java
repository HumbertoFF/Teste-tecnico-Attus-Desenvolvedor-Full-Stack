package com.humberto.api.ocorrencias.repository;

import com.humberto.api.ocorrencias.model.Ocorrencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long>,
        JpaSpecificationExecutor<Ocorrencia> {
  @Query(
    value = """
            SELECT o FROM Ocorrencia o
            JOIN FETCH o.cliente c
            JOIN FETCH o.endereco e
            WHERE (:nmeCliente    IS NULL OR LOWER(c.nmeCliente)  LIKE %:nmeCliente%)
            AND   (:nroCpf        IS NULL OR c.nroCpf             = :nroCpf)
            AND   (:dtaOcorrencia IS NULL OR o.dtaOcorrencia      = :dtaOcorrencia)
            AND   (:nmeCidade     IS NULL OR LOWER(e.nmeCidade)   LIKE %:nmeCidade%)
            """,
    countQuery = """
            SELECT COUNT(o) FROM Ocorrencia o
            JOIN o.cliente c
            JOIN o.endereco e
            WHERE (:nmeCliente    IS NULL OR LOWER(c.nmeCliente)  LIKE %:nmeCliente%)
            AND   (:nroCpf        IS NULL OR c.nroCpf             = :nroCpf)
            AND   (:dtaOcorrencia IS NULL OR o.dtaOcorrencia      = :dtaOcorrencia)
            AND   (:nmeCidade     IS NULL OR LOWER(e.nmeCidade)   LIKE %:nmeCidade%)
            """
  )
  Page<Ocorrencia> buscarComFiltros(
    @Param("nmeCliente")    String nmeCliente,
    @Param("nroCpf")        String nroCpf,
    @Param("dtaOcorrencia") LocalDate dtaOcorrencia,
    @Param("nmeCidade")     String nmeCidade,
    Pageable pageable
  );
}
