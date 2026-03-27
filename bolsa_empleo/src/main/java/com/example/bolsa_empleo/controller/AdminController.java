package com.example.bolsa_empleo.controller;

import com.example.bolsa_empleo.model.*;
import com.example.bolsa_empleo.repository.AdminRepository;
import com.example.bolsa_empleo.repository.EmpresaRepository;
import com.example.bolsa_empleo.repository.CandidatoRepository;
import com.example.bolsa_empleo.repository.CaracteristicaRepository;
import com.example.bolsa_empleo.repository.PuestoRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private AdminRepository adminRepository;
    private PasswordEncoder passwordEncoder;
    private EmpresaRepository empresaRepository;
    private final CandidatoRepository candidatoRepository;
    private final CaracteristicaRepository caracteristicaRepository;
    private final PuestoRepository puestoRepository;

    public AdminController(AdminRepository adminRepository,
                           PasswordEncoder passwordEncoder,
                           EmpresaRepository empresaRepository,
                           CandidatoRepository candidatoRepository,
                           CaracteristicaRepository caracteristicaRepository,
                           PuestoRepository puestoRepository) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.empresaRepository = empresaRepository;
        this.candidatoRepository = candidatoRepository;
        this.caracteristicaRepository = caracteristicaRepository;
        this.puestoRepository = puestoRepository;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "presentation/admin/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String identification,
                        @RequestParam String password,
                        HttpSession session) {

        var adminOpt = adminRepository.findById(identification);

        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();

            if (passwordEncoder.matches(password, admin.getPassword())) {
                session.setAttribute("adminLogueado", admin);
                return "redirect:/admin/dashboard";
            }
        }

        return "redirect:/admin/login?error";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {

        if (session.getAttribute("adminLogueado") == null) {
            return "redirect:/admin/login";
        }

        return "presentation/admin/dashboard";
    }

    // aprobar empresas
    @GetMapping("/empresas")
    public String verEmpresas(HttpSession session, Model model) {

        if (session.getAttribute("adminLogueado") == null) {
            return "redirect:/admin/login";
        }

        var empresas = empresaRepository.findAll();
        model.addAttribute("empresas", empresas);

        return "presentation/admin/empresas";
    }

    @GetMapping("/empresa/{id}/aprobar")
    public String aprobarEmpresa(@PathVariable Long id) {

        Empresa e = empresaRepository.findById(id).orElse(null);

        if (e != null) {
            e.setApproved(true);
            empresaRepository.save(e);
        }

        return "redirect:/admin/empresas";
    }

    // aprobar candidatos
    @GetMapping("/candidatos")
    public String verCandidatos(HttpSession session, Model model) {

        if (session.getAttribute("adminLogueado") == null) {
            return "redirect:/admin/login";
        }

        var candidatos = candidatoRepository.findAll();
        model.addAttribute("candidatos", candidatos);

        return "presentation/admin/candidatos";
    }

    @GetMapping("/candidato/{id}/rechazar")
    public String rechazarCandidato(@PathVariable Long id) {

        Candidato c = candidatoRepository.findById(id).orElse(null);

        if (c != null) {
            candidatoRepository.delete(c);
        }

        return "redirect:/admin/candidatos";
    }

    @GetMapping("/candidato/{id}/aprobar")
    public String aprobarCandidato(@PathVariable Long id) {

        Candidato c = candidatoRepository.findById(id).orElse(null);

        if (c != null) {
            c.setApproved(true);
            candidatoRepository.save(c);
        }

        return "redirect:/admin/candidatos";
    }

    @GetMapping("/empresa/{id}/rechazar")
    public String rechazarEmpresa(@PathVariable Long id) {

        Empresa e = empresaRepository.findById(id).orElse(null);

        if (e != null) {
            empresaRepository.delete(e); // simple
        }

        return "redirect:/admin/empresas";
    }

    // caracteristica
    @GetMapping("/caracteristicas")
    public String verCaracteristicas(Model model) {
        model.addAttribute("caracteristicas", caracteristicaRepository.findAll());
        return "presentation/admin/caracteristicas";
    }

    @PostMapping("/caracteristicas")
    public String crear(@RequestParam String name,
                        @RequestParam(required = false) Long parentId) {

        Caracteristica c = new Caracteristica();
        c.setName(name);

        if (parentId != null) {
            var parent = caracteristicaRepository.findById(parentId).orElse(null);
            c.setParent(parent);
        }

        caracteristicaRepository.save(c);

        return "redirect:/admin/caracteristicas";
    }

    @GetMapping("/reporte")
    public void generarPDF(@RequestParam int year,
                           @RequestParam int month,
                           HttpServletResponse response) throws Exception {

        LocalDateTime inicio = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime fin = inicio.plusMonths(1);

        var puestos = puestoRepository.findByCreatedAtBetween(inicio, fin);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reporte.pdf");

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Reporte de Puestos"));
        document.add(new Paragraph("Mes: " + month + " / Año: " + year));
        document.add(new Paragraph(" "));

        float[] columnas = {200F, 200F, 150F};
        Table table = new Table(columnas);

        table.addHeaderCell("Descripción");
        table.addHeaderCell("Empresa");
        table.addHeaderCell("Fecha");

        for (Puesto p : puestos) {
            table.addCell(p.getDescription());
            table.addCell(p.getEmpresa().getName());
            table.addCell(p.getCreatedAt().toString());
        }

        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total de puestos: " + puestos.size()));

        document.close();
    }
}
