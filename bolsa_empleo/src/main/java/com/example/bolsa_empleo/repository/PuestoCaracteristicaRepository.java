package com.example.bolsa_empleo.repository;

import com.example.bolsa_empleo.model.PuestoCaracteristica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PuestoCaracteristicaRepository extends JpaRepository<PuestoCaracteristica, Long>{
    List<PuestoCaracteristica> findByPuestoId(Long puestoId);
}
