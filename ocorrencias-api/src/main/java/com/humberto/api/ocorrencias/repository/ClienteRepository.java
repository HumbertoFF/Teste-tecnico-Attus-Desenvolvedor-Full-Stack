package com.humberto.api.ocorrencias.repository;

import com.humberto.api.ocorrencias.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByNroCpf(String nroCpf);
    boolean existsByNroCpf(String nroCpf);
}

