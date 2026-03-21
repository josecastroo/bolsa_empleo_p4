package com.example.bolsa_empleo.repository;

import com.example.bolsa_empleo.model.CandidatoCaracteristica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidatoCaracteristicaRepository extends JpaRepository<CandidatoCaracteristica, Long> {
    List<CandidatoCaracteristica> findByCandidatoId(Long candidatoId);
}
