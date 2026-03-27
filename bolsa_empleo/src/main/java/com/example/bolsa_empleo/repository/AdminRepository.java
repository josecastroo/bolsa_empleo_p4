package com.example.bolsa_empleo.repository;

import com.example.bolsa_empleo.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, String> {
}
