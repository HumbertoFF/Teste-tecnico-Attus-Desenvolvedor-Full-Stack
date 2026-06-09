package com.humberto.api.ocorrencias.repository;

import com.humberto.api.ocorrencias.model.FotoOcorrencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FotoOcorrenciaRepository extends JpaRepository<FotoOcorrencia, Long> {
  List<FotoOcorrencia> findByOcorrencia_CodOcorrencia(Long codOcorrencia);
}
