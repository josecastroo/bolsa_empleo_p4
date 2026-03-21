package com.example.bolsa_empleo.controller;

import com.example.bolsa_empleo.model.Empresa;
import com.example.bolsa_empleo.repository.EmpresaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
}
