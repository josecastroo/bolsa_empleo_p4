package com.example.bolsa_empleo.controller;

import com.example.bolsa_empleo.model.*;
import com.example.bolsa_empleo.repository.CaracteristicaRepository;
import com.example.bolsa_empleo.repository.EmpresaRepository;
import com.example.bolsa_empleo.repository.PuestoRepository;
import com.example.bolsa_empleo.repository.PuestoCaracteristicaRepository;
import com.example.bolsa_empleo.repository.CandidatoRepository;
import com.example.bolsa_empleo.repository.AplicacionRepository;
import com.example.bolsa_empleo.service.MatchingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/empresa")
public class EmpresaController {

    private final EmpresaRepository empresaRepository;
    private final PuestoRepository puestoRepository;
    private final CaracteristicaRepository caracteristicaRepository;
    private final PuestoCaracteristicaRepository puestoCaracteristicaRepository;
    private final CandidatoRepository candidatoRepository;
    private final AplicacionRepository aplicacionRepository;
    private final MatchingService matchingService;

    public EmpresaController(EmpresaRepository empresaRepository,
                             PuestoRepository puestoRepository,
                             CaracteristicaRepository caracteristicaRepository,
                             PuestoCaracteristicaRepository puestoCaracteristicaRepository,
                             CandidatoRepository candidatoRepository,
                             AplicacionRepository aplicacionRepository,
                             MatchingService matchingService) {
        this.empresaRepository = empresaRepository;
        this.puestoRepository = puestoRepository;
        this.caracteristicaRepository = caracteristicaRepository;
        this.puestoCaracteristicaRepository = puestoCaracteristicaRepository;
        this.candidatoRepository = candidatoRepository;
        this.aplicacionRepository = aplicacionRepository;
        this.matchingService = matchingService;
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
        var puestos = puestoRepository.findByEmpresaId(empresa.getId());
        model.addAttribute("empresa", empresa);
        model.addAttribute("puestos", puestos);
        return "presentation/empresa/dashboard";
    }

    // crear puesto
    @GetMapping("/puesto/nuevo")
    public String mostrarFormularioPuesto(HttpSession session, Model model) {
        Empresa empresa = (Empresa) session.getAttribute("empresaLogueada");

        if (empresa == null) {
            return "redirect:/empresa/login";
        }
        model.addAttribute("puesto", new Puesto());
        model.addAttribute("caracteristicas", caracteristicaRepository.findAll());

        return "presentation/empresa/crear_puesto";
    }

    @PostMapping("/puesto")
    public String guardarPuesto(@ModelAttribute Puesto puesto,
                                @RequestParam(required = false) List<Long> caracteristicaIds,
                                @RequestParam(required = false) List<Integer> niveles,
                                HttpSession session) {

        Empresa empresa = (Empresa) session.getAttribute("empresaLogueada");

        if (empresa == null) {
            return "redirect:/empresa/login";
        }
        puesto.setEmpresa(empresa);
        puesto.setActive(true);
        puesto.setCreatedAt(java.time.LocalDateTime.now());
        puestoRepository.save(puesto);

        if (caracteristicaIds != null && niveles != null) {
            for (int i = 0; i < caracteristicaIds.size(); i++) {
                Caracteristica c = caracteristicaRepository.findById(caracteristicaIds.get(i)).orElse(null);

                PuestoCaracteristica pc = new PuestoCaracteristica();
                pc.setPuesto(puesto);
                pc.setCaracteristica(c);
                pc.setRequiredLevel(niveles.get(i));

                puestoCaracteristicaRepository.save(pc);
            }
        }
        return "redirect:/empresa/dashboard";
    }

    @GetMapping("/puesto/{id}/candidatos")
    public String verCandidatos(@PathVariable Long id,
                                HttpSession session,
                                Model model) {

        Empresa empresa = (Empresa) session.getAttribute("empresaLogueada");
        if (empresa == null) {
            return "redirect:/empresa/login";
        }

        Puesto puesto = puestoRepository.findById(id).orElse(null);

        if (puesto == null) {
            return "redirect:/empresa/dashboard";
        }
        var candidatos = candidatoRepository.findAll();
        List<Map.Entry<Candidato, Double>> matches = new ArrayList<>();

        for (Candidato c : candidatos) {
            double score = matchingService.calcularMatch(puesto, c);

            if (score > 0) {
                matches.add(Map.entry(c, score));
            }
        }
        matches.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        model.addAttribute("puesto", puesto);
        model.addAttribute("matches", matches);
        return "presentation/empresa/candidatos";
    }

    // empresa ve aplicacion del candidato
    @GetMapping("/puesto/{id}/aplicaciones")
    public String verAplicaciones(@PathVariable Long id,
                                  HttpSession session,
                                  Model model) {

        Empresa empresa = (Empresa) session.getAttribute("empresaLogueada");

        if (empresa == null) {
            return "redirect:/empresa/login";
        }
        Puesto puesto = puestoRepository.findById(id).orElse(null);

        if (puesto == null) {
            return "redirect:/empresa/dashboard";
        }
        var aplicaciones = aplicacionRepository.findByPuesto(puesto);
        model.addAttribute("puesto", puesto);
        model.addAttribute("aplicaciones", aplicaciones);
        return "presentation/empresa/aplicaciones";
    }

    // aceptar aplicacion
    @GetMapping("/aplicacion/{id}/aceptar")
    public String aceptar(@PathVariable Long id) {

        Aplicacion app = aplicacionRepository.findById(id).orElse(null);

        if (app != null) {
            app.setEstado("ACEPTADO");
            aplicacionRepository.save(app);
        }
        return "redirect:/empresa/dashboard";
    }

    // rechazar aplicacion
    @GetMapping("/aplicacion/{id}/rechazar")
    public String rechazar(@PathVariable Long id) {

        Aplicacion app = aplicacionRepository.findById(id).orElse(null);

        if (app != null) {
            app.setEstado("RECHAZADO");
            aplicacionRepository.save(app);
        }
        return "redirect:/empresa/dashboard";
    }
}
