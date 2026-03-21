package com.example.bolsa_empleo.controller;

import com.example.bolsa_empleo.model.Empresa;
import com.example.bolsa_empleo.repository.EmpresaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/empresa")
public class EmpresaController {

    private final EmpresaRepository empresaRepository;

    public EmpresaController(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    // mostrar form
    @GetMapping("/registro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("empresa", new Empresa());
        return "presentation/empresa/registro";
    }

    // modificar form
    @PostMapping("/registro")
    public String registrarEmpresa(@ModelAttribute Empresa empresa) {

        empresa.setApproved(false); // pendiente de aprobación
        empresaRepository.save(empresa);
        return "redirect:/empresa/registro?success";
    }

    // login
    @GetMapping("/login")
    public String mostrarLogin() {
        return "presentation/empresa/login";
    }

    @PostMapping("/login")
    public String loginEmpresa(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session) {

        var empresaOpt = empresaRepository.findByEmailAndPassword(email, password);

        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();

            if (!empresa.isApproved()) {
                return "redirect:/empresa/login?notApproved";
            }
            session.setAttribute("empresaLogueada", empresa);
            return "redirect:/empresa/dashboard";
        }
        return "redirect:/empresa/login?error";
    }

    // dashboard
    @GetMapping("/dashboard")
    public String dashboardEmpresa(HttpSession session, Model model) {

        Empresa empresa = (Empresa) session.getAttribute("empresaLogueada");

        if (empresa == null) {
            return "redirect:/empresa/login";
        }

        model.addAttribute("empresa", empresa);

        return "presentation/empresa/dashboard";
    }
}
