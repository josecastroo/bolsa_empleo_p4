package com.example.bolsa_empleo.repository;

import com.example.bolsa_empleo.model.Aplicacion;
import com.example.bolsa_empleo.model.Candidato;
import com.example.bolsa_empleo.model.Puesto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AplicacionRepository extends JpaRepository<Aplicacion, Long> {

    List<Aplicacion> findByCandidato(Candidato candidato);
    List<Aplicacion> findByPuesto(Puesto puesto);

    boolean existsByCandidatoAndPuesto(Candidato candidato, Puesto puesto);
}
