package com.example.bolsa_empleo.config;

import com.example.bolsa_empleo.model.Admin;
import com.example.bolsa_empleo.repository.AdminRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(AdminRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (!repo.existsById("admin1")) {

                Admin admin = new Admin();
                admin.setIdentification("admin1");
                admin.setPassword(encoder.encode("1234"));
                admin.setName("admin1");
                repo.save(admin);
            }
        };
    }
}