package com.example.bolsa_empleo.controller;

import com.example.bolsa_empleo.model.Puesto;
import com.example.bolsa_empleo.model.TipoPuesto;
import com.example.bolsa_empleo.model.PuestoCaracteristica;
import com.example.bolsa_empleo.repository.PuestoCaracteristicaRepository;
import com.example.bolsa_empleo.repository.PuestoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PublicController {

    private final PuestoRepository puestoRepository;
    private final PuestoCaracteristicaRepository puestoCaracteristicaRepository;

    public PublicController(PuestoRepository puestoRepository,
                            PuestoCaracteristicaRepository puestoCaracteristicaRepository) {
        this.puestoRepository = puestoRepository;
        this.puestoCaracteristicaRepository = puestoCaracteristicaRepository;
    }

    @GetMapping("/")
    public String home(Model model) {

        var puestos = puestoRepository.findAll();

        Map<Long, List<PuestoCaracteristica>> mapa = new HashMap<>();

        for (Puesto p : puestos) {
            var lista = puestoCaracteristicaRepository.findByPuesto(p);
            mapa.put(p.getId(), lista);
        }

        model.addAttribute("puestos", puestos);
        model.addAttribute("mapaCaracteristicas", mapa);

        return "presentation/public/home";
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam(required = false) String keyword,
                         Model model) {

        var resultados = puestoRepository
                .findByTypeAndActiveAndDescription(
                        TipoPuesto.PUBLIC, true, keyword == null ? "" : keyword
                );

        model.addAttribute("puestos", resultados);

        return "presentation/public/buscar";
    }
}
