package com.example.bolsa_empleo.repository;

import com.example.bolsa_empleo.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByEmail(String email);

    Optional<Empresa> findByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);
}
