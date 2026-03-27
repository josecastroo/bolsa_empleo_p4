package com.example.bolsa_empleo.controller;

import com.example.bolsa_empleo.model.Admin;
import com.example.bolsa_empleo.model.Candidato;
import com.example.bolsa_empleo.model.Empresa;
import com.example.bolsa_empleo.repository.CandidatoRepository;
import com.example.bolsa_empleo.repository.EmpresaRepository;
import com.example.bolsa_empleo.repository.AdminRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final CandidatoRepository candidatoRepository;
    private final EmpresaRepository empresaRepository;
    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController( CandidatoRepository candidatoRepository,
                           EmpresaRepository empresaRepository,
                           AdminRepository adminRepository,
                           BCryptPasswordEncoder passwordEncoder) {

        this.candidatoRepository = candidatoRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminRepository = adminRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "presentation/auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        @RequestParam String tipo,
                        HttpSession session) {

        // admin
        if (tipo.equals("admin")) {

            var adminOpt = adminRepository.findById(email);

            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();

                if (passwordEncoder.matches(password, admin.getPassword())) {
                    session.setAttribute("adminLogueado", admin);
                    return "redirect:/admin/dashboard";
                }
            }
        }

        // empresa
        if (tipo.equals("empresa")) {

            var empresaOpt = empresaRepository.findByEmail(email);

            if (empresaOpt.isPresent()) {
                Empresa e = empresaOpt.get();

                if (passwordEncoder.matches(password, e.getPassword())) {

                    if (!e.isApproved()) {
                        return "redirect:/login?notApproved";
                    }

                    session.setAttribute("empresaLogueada", e);
                    return "redirect:/empresa/dashboard";
                }
            }
        }

        // candidato
        if (tipo.equals("candidato")) {

            var candidatoOpt = candidatoRepository.findByEmail(email);

            if (candidatoOpt.isPresent()) {
                Candidato c = candidatoOpt.get();

                if (passwordEncoder.matches(password, c.getPassword())) {
                    session.setAttribute("candidatoLogueado", c);
                    return "redirect:/candidato/dashboard";
                }
            }
        }

        return "redirect:/login?error";
    }
}
