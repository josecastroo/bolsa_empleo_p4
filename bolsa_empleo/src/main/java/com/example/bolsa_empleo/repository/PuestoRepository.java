package com.example.bolsa_empleo.repository;

import com.example.bolsa_empleo.model.Puesto;
import com.example.bolsa_empleo.model.TipoPuesto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PuestoRepository extends JpaRepository<Puesto, Long> {

    List<Puesto> findTop5ByTypeAndActiveOrderByCreatedAtDesc(String type, boolean active);
    List<Puesto> findByEmpresaId(Long empresaId);
    List<Puesto> findTop5ByActive(TipoPuesto type, boolean active);
    List<Puesto> findByTypeAndActiveAndDescription(
            TipoPuesto type, boolean active, String keyword);
    List<Puesto> findByTypeAndActive(String type, boolean active);
    List<Puesto> findByActiveTrue();
    List<Puesto> findByCreatedAtBetween(LocalDateTime inicio, LocalDateTime fin);
}
