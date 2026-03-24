package com.example.bolsa_empleo.controller;

import com.example.bolsa_empleo.model.Candidato;
import com.example.bolsa_empleo.model.Caracteristica;
import com.example.bolsa_empleo.model.CandidatoCaracteristica;
import com.example.bolsa_empleo.model.Puesto;
import com.example.bolsa_empleo.model.Aplicacion;
import com.example.bolsa_empleo.repository.CandidatoRepository;
import com.example.bolsa_empleo.repository.CaracteristicaRepository;
import com.example.bolsa_empleo.repository.CandidatoCaracteristicaRepository;
import com.example.bolsa_empleo.repository.PuestoRepository;
import com.example.bolsa_empleo.repository.AplicacionRepository;
import com.example.bolsa_empleo.service.MatchingService;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/candidato")
public class CandidatoController {

    private final CandidatoRepository candidatoRepository;
    private final CaracteristicaRepository caracteristicaRepository;
    private final CandidatoCaracteristicaRepository candidatoCaracteristicaRepository;
    private final PuestoRepository puestoRepository;
    private final AplicacionRepository aplicacionRepository;
    private final MatchingService matchingService;
    private final BCryptPasswordEncoder passwordEncoder;

    public CandidatoController(CandidatoRepository candidatoRepository,
                               CaracteristicaRepository caracteristicaRepository,
                               CandidatoCaracteristicaRepository candidatoCaracteristicaRepository,
                               PuestoRepository puestoRepository,
                               AplicacionRepository aplicacionRepository,
                               MatchingService matchingService,
                               BCryptPasswordEncoder passwordEncoder) {
        this.candidatoRepository = candidatoRepository;
        this.caracteristicaRepository = caracteristicaRepository;
        this.candidatoCaracteristicaRepository = candidatoCaracteristicaRepository;
        this.puestoRepository = puestoRepository;
        this.matchingService = matchingService;
        this.aplicacionRepository = aplicacionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // registro
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("candidato", new Candidato());
        return "presentation/candidato/registro";
    }

    @PostMapping("/registro")
    public String registrar(@ModelAttribute Candidato candidato) {

        candidato.setPassword(passwordEncoder.encode(candidato.getPassword()));
        candidatoRepository.save(candidato);
        return "redirect:/login";
    }

    // dashboard
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        Candidato candidato = (Candidato) session.getAttribute("candidatoLogueado");

        if (candidato == null) {
            return "redirect:/login";
        }

        var puestos = puestoRepository.findByActiveTrue();

        List<Map.Entry<Puesto, Double>> matches = new ArrayList<>();

        for (Puesto p : puestos) {
            double score = matchingService.calcularMatch(p, candidato);

            if (score > 0) {
                matches.add(Map.entry(p, score));
            }
        }
        matches.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        model.addAttribute("candidato", candidato);
        model.addAttribute("matches", matches);

        return "presentation/candidato/dashboard";
    }

    // habilidades
    @GetMapping("/habilidades")
    public String mostrarHabilidades(HttpSession session, Model model) {

        Candidato candidato = (Candidato) session.getAttribute("candidatoLogueado");

        if (candidato == null) {
            return "redirect:/login";
        }

        model.addAttribute("caracteristicas", caracteristicaRepository.findAll());

        return "presentation/candidato/habilidades";
    }

    @PostMapping("/habilidades")
    public String guardarHabilidades(@RequestParam(required = false) List<Long> caracteristicaIds,
                                     @RequestParam(required = false) List<Integer> niveles,
                                     HttpSession session) {

        Candidato candidato = (Candidato) session.getAttribute("candidatoLogueado");

        if (candidato == null) {
            return "redirect:/login";
        }

        if (caracteristicaIds != null && niveles != null) {
            for (int i = 0; i < caracteristicaIds.size(); i++) {

                Caracteristica c = caracteristicaRepository
                        .findById(caracteristicaIds.get(i))
                        .orElse(null);

                if (c == null) continue;

                CandidatoCaracteristica existente =
                        candidatoCaracteristicaRepository
                                .findByCandidatoAndCaracteristica(candidato, c)
                                .orElse(null);

                if (existente != null) {
                    existente.setLevel(niveles.get(i));
                    candidatoCaracteristicaRepository.save(existente);
                } else {
                    CandidatoCaracteristica cc = new CandidatoCaracteristica();
                    cc.setCandidato(candidato);
                    cc.setCaracteristica(c);
                    cc.setLevel(niveles.get(i));

                    candidatoCaracteristicaRepository.save(cc);
                }
            }
        }

        return "redirect:/candidato/dashboard";
    }

    // aplicar a puesto
    @GetMapping("/aplicar/{id}")
    public String aplicar(@PathVariable Long id, HttpSession session) {

        Candidato candidato = (Candidato) session.getAttribute("candidatoLogueado");

        if (candidato == null) {
            return "redirect:/login";
        }
        Puesto puesto = puestoRepository.findById(id).orElse(null);

        if (puesto == null) {
            return "redirect:/candidato/dashboard";
        }

        if (!puesto.isActive()) {
            return "redirect:/candidato/dashboard?inactivo";
        }
        boolean yaExiste = aplicacionRepository
                .existsByCandidatoAndPuesto(candidato, puesto);

        if (yaExiste) {
            return "redirect:/candidato/dashboard?yaAplico";
        }

        Aplicacion app = new Aplicacion();
        app.setCandidato(candidato);
        app.setPuesto(puesto);
        app.setEstado("PENDIENTE");
        app.setDate(java.time.LocalDateTime.now());
        aplicacionRepository.save(app);

        return "redirect:/candidato/dashboard?success";
    }

    // ver estado de aplicaciones
    @GetMapping("/aplicaciones")
    public String verAplicaciones(HttpSession session, Model model) {

        Candidato candidato = (Candidato) session.getAttribute("candidatoLogueado");

        if (candidato == null) {
            return "redirect:/login";
        }
        var aplicaciones = aplicacionRepository.findByCandidato(candidato);
        model.addAttribute("aplicaciones", aplicaciones);
        return "presentation/candidato/aplicaciones";
    }

    @PostMapping("/cv")
    public String subirCV(@RequestParam("file") MultipartFile file,
                          HttpSession session) throws Exception {

        Candidato candidato = (Candidato) session.getAttribute("candidatoLogueado");

        if (candidato == null) {
            return "redirect:/login";
        }

        if (file.isEmpty()) {
            return "redirect:/candidato/dashboard?error";
        }

        String carpeta = System.getProperty("user.dir") + "/uploads/";

        String original = file.getOriginalFilename();
        if (original == null) original = "cv.pdf";

        String nombreArchivo = candidato.getId() + "_" + original;

        File destino = new File(carpeta + nombreArchivo);
        destino.getParentFile().mkdirs();

        file.transferTo(destino);

        candidato.setCvPath(nombreArchivo);
        candidatoRepository.save(candidato);

        return "redirect:/candidato/dashboard?cvOk";
    }
}
