package com.example.bolsa_empleo.controller;

import com.example.bolsa_empleo.model.*;
import com.example.bolsa_empleo.repository.*;
import com.example.bolsa_empleo.service.MatchingService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final CandidatoCaracteristicaRepository candidatoCaracteristicaRepository;
    private final AplicacionRepository aplicacionRepository;
    private final MatchingService matchingService;
    private final BCryptPasswordEncoder passwordEncoder;

    public EmpresaController(EmpresaRepository empresaRepository,
                             PuestoRepository puestoRepository,
                             CaracteristicaRepository caracteristicaRepository,
                             PuestoCaracteristicaRepository puestoCaracteristicaRepository,
                             CandidatoRepository candidatoRepository,
                             CandidatoCaracteristicaRepository candidatoCaracteristicaRepository,
                             AplicacionRepository aplicacionRepository,
                             MatchingService matchingService,
                             BCryptPasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository;
        this.puestoRepository = puestoRepository;
        this.caracteristicaRepository = caracteristicaRepository;
        this.puestoCaracteristicaRepository = puestoCaracteristicaRepository;
        this.candidatoRepository = candidatoRepository;
        this.candidatoCaracteristicaRepository = candidatoCaracteristicaRepository;
        this.aplicacionRepository = aplicacionRepository;
        this.matchingService = matchingService;
        this.passwordEncoder = passwordEncoder;
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

        empresa.setPassword(passwordEncoder.encode(empresa.getPassword()));
        empresa.setApproved(false); // pendiente de aprobación
        empresaRepository.save(empresa);
        return "redirect:/empresa/registro?success";
    }

    // dashboard
    @GetMapping("/dashboard")
    public String dashboardEmpresa(HttpSession session, Model model) {
        Empresa empresa = (Empresa) session.getAttribute("empresaLogueada");

        if (empresa == null) {
            return "redirect:/login";
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
            return "redirect:/login";
        }
        model.addAttribute("puesto", new Puesto());
        model.addAttribute("caracteristicas", caracteristicaRepository.findAll());

        return "presentation/empresa/crear_puesto";
    }

    @PostMapping("/puesto")
    public String guardarPuesto(@ModelAttribute Puesto puesto,
                                @RequestParam(required = false) List<Long> caracteristicaIds,
                                @RequestParam(required = false) List<Long> seleccionadas,
                                @RequestParam(required = false) List<Integer> niveles,
                                HttpSession session) {

        Empresa empresa = (Empresa) session.getAttribute("empresaLogueada");

        if (empresa == null) {
            return "redirect:/login";
        }

        if (!empresa.isApproved()) {
            return "redirect:/empresa/dashboard?notApproved";
        }

        puesto.setEmpresa(empresa);
        puesto.setActive(true);
        puesto.setCreatedAt(java.time.LocalDateTime.now());
        puestoRepository.save(puesto);

        if (caracteristicaIds != null && niveles != null
                && seleccionadas != null
                && caracteristicaIds.size() == niveles.size()) {

            for (int i = 0; i < caracteristicaIds.size(); i++) {
                Long idCaracteristica = caracteristicaIds.get(i);

                if (!seleccionadas.contains(idCaracteristica)) continue;

                Caracteristica c = caracteristicaRepository
                        .findById(idCaracteristica)
                        .orElse(null);

                if (c == null) continue;

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
                                @RequestParam(required = false) String keyword,
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

        if (puesto.getEmpresa().getId() != empresa.getId()) {
            return "redirect:/empresa/dashboard";
        }

        var candidatos = candidatoRepository.findAll();

        List<Map.Entry<Candidato, Double>> matches = new ArrayList<>();

        for (Candidato c : candidatos) {
            if (keyword != null && !keyword.isEmpty()) {
                String nombre = (c.getFirstName() + " " + c.getLastName()).toLowerCase();

                if (!nombre.contains(keyword.toLowerCase())) {
                    continue;
                }
            }
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

    @GetMapping("/candidato/{id}")
    public String verCandidato(@PathVariable Long id,
                               HttpSession session,
                               Model model) {

        Empresa empresa = (Empresa) session.getAttribute("empresaLogueada");

        if (empresa == null) {
            return "redirect:/empresa/login";
        }

        Candidato candidato = candidatoRepository.findById(id).orElse(null);

        if (candidato == null) {
            return "redirect:/empresa/dashboard";
        }

        var habilidades = candidatoCaracteristicaRepository.findByCandidato(candidato);

        model.addAttribute("candidato", candidato);
        model.addAttribute("habilidades", habilidades);

        return "presentation/empresa/candidato_detalle";
    }

    // empresa ve aplicacion del candidato
    @GetMapping("/puesto/{id}/aplicaciones")
    public String verAplicaciones(@PathVariable Long id,
                                  @RequestParam(required = false) String keyword,
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

        if (!(puesto.getEmpresa().getId() == empresa.getId())) {
            return "redirect:/empresa/dashboard";
        }

        var aplicaciones = aplicacionRepository.findByPuestoId(puesto.getId());

        aplicaciones = aplicaciones.stream()
                .filter(app -> !app.getEstado().equals("RECHAZADO"))
                .toList();

        if (keyword != null && !keyword.isEmpty()) {
            aplicaciones = aplicaciones.stream()
                    .filter(app -> {
                        var c = app.getCandidato();
                        if (c == null) return false;
                        String nombre = (c.getFirstName() + " " + c.getLastName()).toLowerCase();
                        return nombre.contains(keyword.toLowerCase());
                    })
                    .toList();
        }

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

    // desactivar puesto
    @GetMapping("/puesto/{id}/toggle")
    public String togglePuesto(@PathVariable Long id, HttpSession session) {

        Empresa empresa = (Empresa) session.getAttribute("empresaLogueada");

        if (empresa == null) {
            return "redirect:/login";
        }
        Puesto puesto = puestoRepository.findById(id).orElse(null);

        if (puesto == null) {
            return "redirect:/empresa/dashboard";
        }
        if (!(puesto.getEmpresa().getId() == empresa.getId())) {
            return "redirect:/empresa/dashboard";
        }
        puesto.setActive(!puesto.isActive());
        puestoRepository.save(puesto);
        return "redirect:/empresa/dashboard";
    }
}
