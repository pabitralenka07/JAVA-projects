package com.portfolio.resume.config;

import com.portfolio.resume.dto.ResumeFormDto;
import com.portfolio.resume.model.ResumeTemplate;
import com.portfolio.resume.service.ResumeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ResumeService resumeService;

    public DataInitializer(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @Override
    public void run(String... args) {
        if (!resumeService.getAllResumes().isEmpty()) {
            return;
        }

        ResumeFormDto seedResume = new ResumeFormDto();
        seedResume.setTitle("Software Engineer - Backend & Cloud");
        seedResume.setTemplate(ResumeTemplate.MODERN_TECH);
        seedResume.setFullName("Alex Mercer");
        seedResume.setHeadline("Full-Stack Java Engineer | Core Java, Spring Boot 3 & System Design");
        seedResume.setEmail("alex.mercer@cs.tech.edu");
        seedResume.setPhone("+1 (555) 382-9012");
        seedResume.setLocation("San Francisco, CA");
        seedResume.setGithubUrl("github.com/alex-mercer-dev");
        seedResume.setLinkedinUrl("linkedin.com/in/alex-mercer-swe");
        seedResume.setPortfolioUrl("alexmercer.dev");

        seedResume.setSummary(
                "Passionate 3rd-year Computer Science undergraduate with a deep focus on Java 17+, " +
                "clean Object-Oriented Architecture, and scalable Spring Boot systems. Proven track record " +
                "designing multi-threaded applications, constraint optimization engines, and zero-setup " +
                "database architectures tested with JUnit 5."
        );

        // Education
        seedResume.setEducations(List.of(
                new ResumeFormDto.EducationDto(
                        "Apex Institute of Technology",
                        "Bachelor of Technology",
                        "Computer Science & Engineering",
                        "2023",
                        "2027",
                        "CGPA 9.35 / 10.0"
                )
        ));

        // Experience
        seedResume.setExperiences(List.of(
                new ResumeFormDto.ExperienceDto(
                        "CloudScale Systems Inc.",
                        "Software Engineering Intern",
                        "San Francisco, CA",
                        "May 2025",
                        "Aug 2025",
                        false,
                        "• Engineered high-performance Spring Boot 3 microservices handling 25,000+ daily telemetry requests\n" +
                        "• Optimized JPA queries with custom fetch joins, eliminating N+1 performance bottlenecks and cutting latency by 42%\n" +
                        "• Implemented zero-install H2 and production MySQL profiles with Docker Compose integration\n" +
                        "• Wrote comprehensive test suites with JUnit 5 and MockMvc achieving over 90% branch coverage"
                ),
                new ResumeFormDto.ExperienceDto(
                        "Department of Computer Science",
                        "Undergraduate Teaching Assistant - Object-Oriented Programming",
                        "Campus",
                        "Jan 2025",
                        "May 2025",
                        false,
                        "• Mentored 120+ students on Core Java, SOLID principles, exception hierarchies, and multi-threading\n" +
                        "• Reviewed weekly code submissions emphasizing design patterns, clean separation of concerns, and Git conventions"
                )
        ));

        // Projects
        seedResume.setProjects(List.of(
                new ResumeFormDto.ProjectDto(
                        "Java Projects 10-Module Portfolio",
                        "Java 17, Spring Boot 3, JPA, OpenPDF, Swing",
                        "Architected a production-ready Maven multi-module repository covering cryptographic generators, lexical analyzers, 2D game loops, JDBC billing, and automated survey engines.",
                        "github.com/alex-mercer-dev/java-projects"
                ),
                new ResumeFormDto.ProjectDto(
                        "Constraint-Satisfaction Seating Solver",
                        "Core Java, CSP Algorithms, OpenPDF",
                        "Engineered a room allocation algorithm enforcing orthogonal non-adjacent same-subject constraints for large-scale examination halls with PDF/CSV export.",
                        "github.com/alex-mercer-dev/exam-seating"
                )
        ));

        // Skills
        seedResume.setSkillCategories(List.of(
                new ResumeFormDto.SkillCategoryDto("Languages", "Java 17/21 (LTS), SQL, Python, TypeScript, Bash"),
                new ResumeFormDto.SkillCategoryDto("Frameworks & Backend", "Spring Boot 3, Spring Data JPA, Hibernate ORM, JUnit 5, Mockito"),
                new ResumeFormDto.SkillCategoryDto("Tools & Infrastructure", "Maven, Docker, H2 Database, MySQL 8.4, Git, OpenPDF, Linux")
        ));

        // Certifications
        seedResume.setCertifications(List.of(
                new ResumeFormDto.CertificationDto("Oracle Certified Professional: Java SE 17 Developer", "Oracle", "2025", ""),
                new ResumeFormDto.CertificationDto("AWS Certified Solutions Architect - Associate", "Amazon Web Services", "2025", "")
        ));

        resumeService.createResume(seedResume);
    }
}
