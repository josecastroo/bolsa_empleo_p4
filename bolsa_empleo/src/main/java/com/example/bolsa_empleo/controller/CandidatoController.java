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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    public CandidatoController(CandidatoRepository candidatoRepository,
                               CaracteristicaRepository caracteristicaRepository,
                               CandidatoCaracteristicaRepository candidatoCaracteristicaRepository,
                               PuestoRepository puestoRepository,
                               AplicacionRepository aplicacionRepository,
                               MatchingService matchingService) {
        this.candidatoRepository = candidatoRepository;
        this.caracteristicaRepository = caracteristicaRepository;
        this.candidatoCaracteristicaRepository = candidatoCaracteristicaRepository;
        this.puestoRepository = puestoRepository;
        this.matchingService = matchingService;
        this.aplicacionRepository = aplicacionRepository;
    }

    // registro
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("candidato", new Candidato());
        return "presentation/candidato/registro";
    }

    @PostMapping("/registro")
    public String registrar(@ModelAttribute Candidato candidato) {
        candidatoRepository.save(candidato);
        return "redirect:/candidato/login";
    }

    // login
    @GetMapping("/login")
    public String mostrarLogin() {
        return "presentation/candidato/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session) {

        var candidatoOpt = candidatoRepository.findByEmailAndPassword(email, password);

        if (candidatoOpt.isPresent()) {
            session.setAttribute("candidatoLogueado", candidatoOpt.get());
            return "redirect:/candidato/dashboard";
        }

        return "redirect:/candidato/login?error";
    }

    // dashboard
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        Candidato candidato = (Candidato) session.getAttribute("candidatoLogueado");

        if (candidato == null) {
            return "redirect:/candidato/login";
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
            return "redirect:/candidato/login";
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
            return "redirect:/candidato/login";
        }

        if (caracteristicaIds != null && niveles != null) {
            for (int i = 0; i < caracteristicaIds.size(); i++) {

                Caracteristica c = caracteristicaRepository
                        .findById(caracteristicaIds.get(i))
                        .orElse(null);

                CandidatoCaracteristica cc = new CandidatoCaracteristica();
                cc.setCandidato(candidato);
                cc.setCaracteristica(c);
                cc.setLevel(niveles.get(i));

                candidatoCaracteristicaRepository.save(cc);
            }
        }

        return "redirect:/candidato/dashboard";
    }

    // aplicar a puesto
    @GetMapping("/aplicar/{id}")
    public String aplicar(@PathVariable Long id, HttpSession session) {

        Candidato candidato = (Candidato) session.getAttribute("candidatoLogueado");

        if (candidato == null) {
            return "redirect:/candidato/login";
        }
        Puesto puesto = puestoRepository.findById(id).orElse(null);

        if (puesto == null) {
            return "redirect:/candidato/dashboard";
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
            return "redirect:/candidato/login";
        }
        var aplicaciones = aplicacionRepository.findByCandidato(candidato);
        model.addAttribute("aplicaciones", aplicaciones);
        return "presentation/candidato/aplicaciones";
    }
}
