package com.example.bolsa_empleo.controller;

import com.example.bolsa_empleo.repository.CandidatoRepository;
import com.example.bolsa_empleo.repository.EmpresaRepository;
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
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController( CandidatoRepository candidatoRepository,
                           EmpresaRepository empresaRepository,
                           BCryptPasswordEncoder passwordEncoder ) {

        this.candidatoRepository = candidatoRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "presentation/auth/login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String email,
                                @RequestParam String password,
                                @RequestParam String tipo,
                                HttpSession session) {

        if (tipo.equals("candidato")) {

            var candidatoOpt = candidatoRepository.findByEmail(email);

            if (candidatoOpt.isPresent()) {
                var c = candidatoOpt.get();

                if (passwordEncoder.matches(password, c.getPassword())) {
                    session.setAttribute("candidatoLogueado", c);
                    return "redirect:/candidato/dashboard";
                }
            }
        } else if (tipo.equals("empresa")) {

            var empresaOpt = empresaRepository.findByEmail(email);

            if (empresaOpt.isPresent()) {
                var e = empresaOpt.get();

                if (passwordEncoder.matches(password, e.getPassword())) {
                    session.setAttribute("empresaLogueada", e);
                    return "redirect:/empresa/dashboard";
                }
            }
        }
        return "redirect:/login?error";
    }
}
