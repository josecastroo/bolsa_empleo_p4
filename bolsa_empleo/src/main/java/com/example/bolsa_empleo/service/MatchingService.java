package com.example.bolsa_empleo.service;

import com.example.bolsa_empleo.model.*;
import com.example.bolsa_empleo.repository.*;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        if (reqs == null || reqs.isEmpty()) return 0;

        int cumple = 0;
        Set<Long> yaContadas = new HashSet<>();

        for (PuestoCaracteristica req : reqs) {
            for (CandidatoCaracteristica skill : skills) {
                Long idReq = req.getCaracteristica().getId();
                Long idSkill = skill.getCaracteristica().getId();

                if (idReq.equals(idSkill) && !yaContadas.contains(idReq)) {
                    if (skill.getLevel() >= req.getRequiredLevel()) {
                        cumple++;
                        yaContadas.add(idReq);
                    }
                }
            }
        }
        return (double) cumple / reqs.size() * 100;
    }
}
