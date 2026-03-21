package com.example.bolsa_empleo.repository;

import com.example.bolsa_empleo.model.Puesto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PuestoRepository extends JpaRepository<Puesto, Long> {

    List<Puesto> findTop5ByTypeAndActiveOrderByCreatedAtDesc(String type, boolean active);

    List<Puesto> findByEmpresaId(Long empresaId);

    List<Puesto> findByTypeAndActive(String type, boolean active);
}
