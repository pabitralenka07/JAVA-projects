package com.portfolio.resume.service;

import com.portfolio.resume.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PdfResumeServiceTest {

    private PdfResumeService pdfResumeService;
    private Resume sampleResume;

    @BeforeEach
    void setUp() {
        pdfResumeService = new PdfResumeService();

        sampleResume = new Resume("Full-Stack CV", "Alex Mercer", "alex@example.com");
        sampleResume.setHeadline("Full-Stack Java Engineer");
        sampleResume.setPhone("+1-555-0199");
        sampleResume.setLocation("San Francisco, CA");
        sampleResume.setGithubUrl("github.com/alex-mercer");
        sampleResume.setLinkedinUrl("linkedin.com/in/alex-mercer");
        sampleResume.setSummary("Software engineer experienced in high-throughput Java microservices.");

        sampleResume.addEducation(new EducationItem("Apex Institute", "B.Tech", "CS", "2023", "2027", "9.2/10", 1));
        sampleResume.addExperience(new ExperienceItem("CloudScale", "Intern", "SF", "May 2025", "Aug 2025", false, "• Reduced latency by 35%\n• Built Spring Boot microservices", 1));
        sampleResume.addProject(new ProjectItem("Portfolio Multi-Module", "Java 17, Spring Boot", "10-project repository", "github.com/alex/portfolio", 1));
        sampleResume.addSkillCategory(new SkillCategory("Languages", "Java 17, SQL, Python", 1));
        sampleResume.addCertification(new CertificationItem("Oracle Certified Java SE", "Oracle", "2025", "", 1));
    }

    @Test
    @DisplayName("Should generate valid PDF bytes for Modern Tech template")
    void testGenerateModernTechPdf() {
        sampleResume.setTemplate(ResumeTemplate.MODERN_TECH);
        byte[] pdfBytes = pdfResumeService.generatePdfResume(sampleResume);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(1000);

        // Verify PDF Magic Bytes (%PDF-)
        String header = new String(pdfBytes, 0, 8, StandardCharsets.US_ASCII);
        assertThat(header).startsWith("%PDF-");
    }

    @Test
    @DisplayName("Should generate valid PDF bytes for Executive template")
    void testGenerateExecutivePdf() {
        sampleResume.setTemplate(ResumeTemplate.EXECUTIVE);
        byte[] pdfBytes = pdfResumeService.generatePdfResume(sampleResume);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(1000);

        String header = new String(pdfBytes, 0, 8, StandardCharsets.US_ASCII);
        assertThat(header).startsWith("%PDF-");
    }
}
