package com.portfolio.resume.service;

import com.portfolio.resume.dto.ResumeFormDto;
import com.portfolio.resume.model.*;
import com.portfolio.resume.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @InjectMocks
    private ResumeService resumeService;

    private Resume sampleResume;

    @BeforeEach
    void setUp() {
        sampleResume = new Resume("SWE Resume", "Alex Mercer", "alex@example.com");
        sampleResume.setId(1L);
        sampleResume.setHeadline("Backend Software Engineer");
        sampleResume.setTemplate(ResumeTemplate.MODERN_TECH);

        sampleResume.addEducation(new EducationItem("Tech University", "B.Tech", "CS", "2023", "2027", "CGPA 9.0", 1));
        sampleResume.addExperience(new ExperienceItem("Alpha Corp", "SWE Intern", "Remote", "May 2025", "Aug 2025", false, "• Impact 1\n• Impact 2", 1));
        sampleResume.addProject(new ProjectItem("Task Manager", "Java, Spring Boot", "Engineered task scheduler", "github.com/alex/task", 1));
        sampleResume.addSkillCategory(new SkillCategory("Languages", "Java, SQL", 1));
    }

    @Test
    @DisplayName("Should create resume and properly cascade all child sections")
    void testCreateResume() {
        ResumeFormDto form = new ResumeFormDto();
        form.setTitle("Cloud Engineer CV");
        form.setFullName("Jane Doe");
        form.setEmail("jane@example.com");
        form.setTemplate(ResumeTemplate.MODERN_TECH);

        form.setEducations(List.of(new ResumeFormDto.EducationDto("MIT", "M.S.", "AI", "2024", "2026", "3.9")));
        form.setExperiences(List.of(new ResumeFormDto.ExperienceDto("Google", "SWE", "Mountain View", "2024", "Present", true, "• Built cloud services")));
        form.setProjects(List.of(new ResumeFormDto.ProjectDto("Cache Engine", "Go, Redis", "In-memory cache", "github.com/jane/cache")));
        form.setSkillCategories(List.of(new ResumeFormDto.SkillCategoryDto("Cloud", "AWS, GCP, Kubernetes")));

        when(resumeRepository.save(any(Resume.class))).thenAnswer(invocation -> {
            Resume r = invocation.getArgument(0);
            r.setId(50L);
            return r;
        });

        Resume created = resumeService.createResume(form);

        assertThat(created).isNotNull();
        assertThat(created.getFullName()).isEqualTo("Jane Doe");
        assertThat(created.getEducations()).hasSize(1);
        assertThat(created.getExperiences()).hasSize(1);
        assertThat(created.getProjects()).hasSize(1);
        assertThat(created.getSkillCategories()).hasSize(1);
    }

    @Test
    @DisplayName("Should update existing resume and refresh child collections")
    void testUpdateResume() {
        when(resumeRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(sampleResume));
        when(resumeRepository.save(any(Resume.class))).thenAnswer(i -> i.getArgument(0));

        ResumeFormDto updateForm = new ResumeFormDto();
        updateForm.setId(1L);
        updateForm.setTitle("Senior SWE Resume");
        updateForm.setFullName("Alex Mercer Sr.");
        updateForm.setEmail("alex.sr@example.com");
        updateForm.setTemplate(ResumeTemplate.EXECUTIVE);
        updateForm.setSkillCategories(List.of(new ResumeFormDto.SkillCategoryDto("Core", "Java 21, Spring Boot 3")));

        Resume updated = resumeService.updateResume(1L, updateForm);

        assertThat(updated.getTitle()).isEqualTo("Senior SWE Resume");
        assertThat(updated.getFullName()).isEqualTo("Alex Mercer Sr.");
        assertThat(updated.getTemplate()).isEqualTo(ResumeTemplate.EXECUTIVE);
        assertThat(updated.getSkillCategoryList()).hasSize(1);
        assertThat(updated.getSkillCategoryList().get(0).getCategoryName()).isEqualTo("Core");
    }

    @Test
    @DisplayName("Should convert entity to form DTO accurately")
    void testToFormDtoMapping() {
        ResumeFormDto dto = resumeService.toFormDto(sampleResume);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFullName()).isEqualTo("Alex Mercer");
        assertThat(dto.getEducations()).hasSize(1);
        assertThat(dto.getExperiences()).hasSize(1);
        assertThat(dto.getProjects()).hasSize(1);
        assertThat(dto.getSkillCategories()).hasSize(1);
    }

    @Test
    @DisplayName("Should delete resume when exists or throw NoSuchElementException")
    void testDeleteResume() {
        when(resumeRepository.existsById(1L)).thenReturn(true);
        when(resumeRepository.existsById(99L)).thenReturn(false);

        resumeService.deleteResume(1L);
        verify(resumeRepository, times(1)).deleteById(1L);

        assertThatThrownBy(() -> resumeService.deleteResume(99L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
