package com.portfolio.resume.controller;

import com.portfolio.resume.dto.ResumeFormDto;
import com.portfolio.resume.model.Resume;
import com.portfolio.resume.model.ResumeTemplate;
import com.portfolio.resume.service.PdfResumeService;
import com.portfolio.resume.service.ResumeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ResumeController {

    private final ResumeService resumeService;
    private final PdfResumeService pdfResumeService;

    public ResumeController(ResumeService resumeService, PdfResumeService pdfResumeService) {
        this.resumeService = resumeService;
        this.pdfResumeService = pdfResumeService;
    }

    @GetMapping({"/", "/resumes"})
    public String listResumes(Model model) {
        List<Resume> resumes = resumeService.getAllResumes();
        model.addAttribute("resumes", resumes);
        return "index";
    }

    @GetMapping("/resumes/new")
    public String newResumeForm(Model model) {
        ResumeFormDto form = new ResumeFormDto();
        // Pre-populate with standard sample slots for intuitive editing
        form.getEducations().add(new ResumeFormDto.EducationDto("", "B.Tech Computer Science", "", "2023", "2027", "CGPA 9.2/10"));
        form.getExperiences().add(new ResumeFormDto.ExperienceDto("", "Software Engineering Intern", "", "June 2025", "Aug 2025", false, "• Optimized API queries reducing latency by 35%\n• Built Spring Boot microservices"));
        form.getProjects().add(new ResumeFormDto.ProjectDto("Multi-Module Portfolio", "Java 17, Spring Boot 3, JPA", "Engineered 10 standalone production-grade modules demonstrating OOP and system design.", "github.com/my-profile/portfolio"));
        form.getSkillCategories().add(new ResumeFormDto.SkillCategoryDto("Languages", "Java 17/21, SQL, Python, TypeScript"));
        form.getSkillCategories().add(new ResumeFormDto.SkillCategoryDto("Frameworks & Tools", "Spring Boot 3, JPA/Hibernate, Docker, Git, JUnit 5"));
        form.getCertifications().add(new ResumeFormDto.CertificationDto("Oracle Certified Associate Java SE", "Oracle", "2025", ""));

        model.addAttribute("resumeForm", form);
        model.addAttribute("templates", ResumeTemplate.values());
        return "resume/form";
    }

    @GetMapping("/resumes/{id}/edit")
    public String editResumeForm(@PathVariable("id") Long id, Model model) {
        Resume resume = resumeService.getResumeById(id);
        ResumeFormDto form = resumeService.toFormDto(resume);
        model.addAttribute("resumeForm", form);
        model.addAttribute("templates", ResumeTemplate.values());
        return "resume/form";
    }

    @PostMapping("/resumes")
    public String saveResume(@Valid @ModelAttribute("resumeForm") ResumeFormDto form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("templates", ResumeTemplate.values());
            return "resume/form";
        }

        try {
            Resume saved;
            if (form.getId() != null) {
                saved = resumeService.updateResume(form.getId(), form);
                redirectAttributes.addFlashAttribute("successMessage", "Resume updated successfully!");
            } else {
                saved = resumeService.createResume(form);
                redirectAttributes.addFlashAttribute("successMessage", "Resume created successfully!");
            }
            return "redirect:/resumes/" + saved.getId();
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Error saving resume: " + ex.getMessage());
            model.addAttribute("templates", ResumeTemplate.values());
            return "resume/form";
        }
    }

    @GetMapping("/resumes/{id}")
    public String viewResumePreview(@PathVariable("id") Long id, Model model) {
        Resume resume = resumeService.getResumeById(id);
        model.addAttribute("resume", resume);
        return "resume/preview";
    }

    @GetMapping("/resumes/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable("id") Long id) {
        Resume resume = resumeService.getResumeById(id);
        byte[] pdfBytes = pdfResumeService.generatePdfResume(resume);

        String safeFileName = resume.getFullName().replaceAll("[^a-zA-Z0-9_-]", "_") + "_Resume.pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeFileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/resumes/{id}/delete")
    public String deleteResume(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        resumeService.deleteResume(id);
        redirectAttributes.addFlashAttribute("successMessage", "Resume deleted successfully.");
        return "redirect:/resumes";
    }
}
