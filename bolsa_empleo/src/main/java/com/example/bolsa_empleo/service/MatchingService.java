package com.example.bolsa_empleo.service;

import com.example.bolsa_empleo.model.*;
import com.example.bolsa_empleo.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchingService {

    private final PuestoCaracteristicaRepository puestoCaracteristicaRepository;
    private final CandidatoCaracteristicaRepository candidatoCaracteristicaRepository;

    public MatchingService(PuestoCaracteristicaRepository puestoCaracteristicaRepository,
                           CandidatoCaracteristicaRepository candidatoCaracteristicaRepository) {
        this.puestoCaracteristicaRepository = puestoCaracteristicaRepository;
        this.candidatoCaracteristicaRepository = candidatoCaracteristicaRepository;
    }

    public double calcularMatch(Puesto puesto, Candidato candidato) {

        List<PuestoCaracteristica> reqs =
                puestoCaracteristicaRepository.findByPuesto(puesto);

        List<CandidatoCaracteristica> skills =
                candidatoCaracteristicaRepository.findByCandidato(candidato);

        if (reqs.isEmpty()) return 0;

        int cumple = 0;

        for (PuestoCaracteristica req : reqs) {
            for (CandidatoCaracteristica skill : skills) {

                if (req.getCaracteristica().getId() == skill.getCaracteristica().getId()) {

                    if (skill.getLevel() >= req.getRequiredLevel()) {
                        cumple++;
                    }
                }
            }
        }

        return (double) cumple / reqs.size() * 100;
    }
}
