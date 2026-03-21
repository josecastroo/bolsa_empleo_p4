package com.example.bolsa_empleo.repository;

import com.example.bolsa_empleo.model.Caracteristica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaracteristicaRepository extends JpaRepository<Caracteristica, Long> {

    List<Caracteristica> findByParentIsNull();

    List<Caracteristica> findByParentId(Long parentId);
}
