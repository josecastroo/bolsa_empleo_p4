package com.example.bolsa_empleo.repository;

import com.example.bolsa_empleo.model.Candidato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidatoRepository extends JpaRepository<Candidato, Long> {

    Optional<Candidato> findByEmail(String email);
    Optional<Candidato> existsByEmail(String email);
}
