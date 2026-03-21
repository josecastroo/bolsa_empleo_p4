package com.example.bolsa_empleo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PuestoCaracteristicaRepository extends JpaRepository<PuestoCaracteristicaRepository, Long>{
    List<PuestoCaracteristicaRepository> findByPuestoId(Long puestoId);
}
