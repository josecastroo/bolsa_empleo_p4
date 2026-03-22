package com.example.bolsa_empleo.repository;

import com.example.bolsa_empleo.model.CandidatoCaracteristica;
import com.example.bolsa_empleo.model.Candidato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidatoCaracteristicaRepository extends JpaRepository<CandidatoCaracteristica, Long> {

    List<CandidatoCaracteristica> findByCandidatoId(Long candidatoId);
    List<CandidatoCaracteristica> findByCandidato(Candidato candidato);
}
