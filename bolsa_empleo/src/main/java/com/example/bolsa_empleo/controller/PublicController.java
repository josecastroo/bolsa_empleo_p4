package com.example.bolsa_empleo.controller;

import com.example.bolsa_empleo.model.Puesto;
import com.example.bolsa_empleo.model.PuestoCaracteristica;
import com.example.bolsa_empleo.repository.CaracteristicaRepository;
import com.example.bolsa_empleo.repository.PuestoCaracteristicaRepository;
import com.example.bolsa_empleo.repository.PuestoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PublicController {

    private final PuestoRepository puestoRepository;
    private final PuestoCaracteristicaRepository puestoCaracteristicaRepository;
    private final CaracteristicaRepository caracteristicaRepository;

    public PublicController(PuestoRepository puestoRepository,
                            PuestoCaracteristicaRepository puestoCaracteristicaRepository,
                            CaracteristicaRepository caracteristicaRepository) {
        this.puestoRepository = puestoRepository;
        this.puestoCaracteristicaRepository = puestoCaracteristicaRepository;
        this.caracteristicaRepository = caracteristicaRepository;
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
        model.addAttribute("caracteristicas", caracteristicaRepository.findAll());

        return "presentation/public/home";
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) Long caracteristicaId,
                         Model model) {

        List<Puesto> puestos = puestoRepository.findAll();

        if (keyword != null && !keyword.isEmpty()) {
            puestos = puestos.stream()
                    .filter(p -> p.getDescription().toLowerCase()
                            .contains(keyword.toLowerCase()))
                    .toList();
        }

        if (caracteristicaId != null) {
            puestos = puestos.stream()
                    .filter(p -> {
                        var pcs = puestoCaracteristicaRepository.findByPuesto(p);
                        return pcs.stream().anyMatch(pc ->
                                pc.getCaracteristica() != null &&
                                        pc.getCaracteristica().getId() == caracteristicaId
                        );
                    })
                    .toList();
        }

        Map<Long, List<PuestoCaracteristica>> mapa = new HashMap<>();

        for (Puesto p : puestos) {
            var lista = puestoCaracteristicaRepository.findByPuesto(p);
            mapa.put(p.getId(), lista);
        }

        model.addAttribute("puestos", puestos);
        model.addAttribute("caracteristicas", caracteristicaRepository.findAll());
        model.addAttribute("mapaCaracteristicas", mapa);

        return "presentation/public/home";
    }
}
