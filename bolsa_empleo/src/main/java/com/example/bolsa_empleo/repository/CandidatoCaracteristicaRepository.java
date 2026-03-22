package com.example.bolsa_empleo.repository;

import com.example.bolsa_empleo.model.CandidatoCaracteristica;
import com.example.bolsa_empleo.model.Candidato;
import com.example.bolsa_empleo.model.Caracteristica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CandidatoCaracteristicaRepository extends JpaRepository<CandidatoCaracteristica, Long> {

    List<CandidatoCaracteristica> findByCandidatoId(Long candidatoId);
    List<CandidatoCaracteristica> findByCandidato(Candidato candidato);

    Optional<CandidatoCaracteristica> findByCandidatoAndCaracteristica(
            Candidato candidato,
            Caracteristica caracteristica
    );
}
