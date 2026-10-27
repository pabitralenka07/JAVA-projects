package com.portfolio.resume.service;

import com.portfolio.resume.dto.ResumeFormDto;
import com.portfolio.resume.model.*;
import com.portfolio.resume.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ResumeService {

    private final ResumeRepository resumeRepository;

    public ResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    @Transactional(readOnly = true)
    public List<Resume> getAllResumes() {
        return resumeRepository.findAllByOrderByUpdatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Resume getResumeById(Long id) {
        return resumeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NoSuchElementException("Resume not found with ID: " + id));
    }

    public Resume createResume(ResumeFormDto dto) {
        Resume resume = new Resume(dto.getTitle(), dto.getFullName(), dto.getEmail());
        applyDtoToEntity(dto, resume);
        return resumeRepository.save(resume);
    }

    public Resume updateResume(Long id, ResumeFormDto dto) {
        Resume resume = resumeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NoSuchElementException("Resume not found with ID: " + id));

        resume.setTitle(dto.getTitle());
        resume.setTemplate(dto.getTemplate());
        resume.setFullName(dto.getFullName());
        resume.setEmail(dto.getEmail());

        // Clear existing child collections before re-populating
        resume.getEducations().clear();
        resume.getExperiences().clear();
        resume.getProjects().clear();
        resume.getSkillCategories().clear();
        resume.getCertifications().clear();

        applyDtoToEntity(dto, resume);
        return resumeRepository.save(resume);
    }

    public void deleteResume(Long id) {
        if (!resumeRepository.existsById(id)) {
            throw new NoSuchElementException("Resume not found with ID: " + id);
        }
        resumeRepository.deleteById(id);
    }

    private void applyDtoToEntity(ResumeFormDto dto, Resume resume) {
        resume.setTemplate(dto.getTemplate() != null ? dto.getTemplate() : ResumeTemplate.MODERN_TECH);
        resume.setHeadline(dto.getHeadline());
        resume.setPhone(dto.getPhone());
        resume.setLocation(dto.getLocation());
        resume.setGithubUrl(dto.getGithubUrl());
        resume.setLinkedinUrl(dto.getLinkedinUrl());
        resume.setPortfolioUrl(dto.getPortfolioUrl());
        resume.setSummary(dto.getSummary());

        if (dto.getEducations() != null) {
            int order = 1;
            for (ResumeFormDto.EducationDto edu : dto.getEducations()) {
                if (edu.getInstitution() != null && !edu.getInstitution().isBlank()) {
                    resume.addEducation(new EducationItem(
                            edu.getInstitution().trim(),
                            edu.getDegree() != null ? edu.getDegree().trim() : "",
                            edu.getFieldOfStudy() != null ? edu.getFieldOfStudy().trim() : "",
                            edu.getStartDate(),
                            edu.getEndDate(),
                            edu.getGrade(),
                            order++
                    ));
                }
            }
        }

        if (dto.getExperiences() != null) {
            int order = 1;
            for (ResumeFormDto.ExperienceDto exp : dto.getExperiences()) {
                if (exp.getCompany() != null && !exp.getCompany().isBlank()) {
                    resume.addExperience(new ExperienceItem(
                            exp.getCompany().trim(),
                            exp.getRole() != null ? exp.getRole().trim() : "",
                            exp.getLocation(),
                            exp.getStartDate(),
                            exp.getEndDate(),
                            exp.isCurrentRole(),
                            exp.getBulletPoints(),
                            order++
                    ));
                }
            }
        }

        if (dto.getProjects() != null) {
            int order = 1;
            for (ResumeFormDto.ProjectDto proj : dto.getProjects()) {
                if (proj.getTitle() != null && !proj.getTitle().isBlank()) {
                    resume.addProject(new ProjectItem(
                            proj.getTitle().trim(),
                            proj.getTechnologies(),
                            proj.getDescription(),
                            proj.getProjectUrl(),
                            order++
                    ));
                }
            }
        }

        if (dto.getSkillCategories() != null) {
            int order = 1;
            for (ResumeFormDto.SkillCategoryDto cat : dto.getSkillCategories()) {
                if (cat.getCategoryName() != null && !cat.getCategoryName().isBlank() && cat.getSkillsCsv() != null) {
                    resume.addSkillCategory(new SkillCategory(
                            cat.getCategoryName().trim(),
                            cat.getSkillsCsv().trim(),
                            order++
                    ));
                }
            }
        }

        if (dto.getCertifications() != null) {
            int order = 1;
            for (ResumeFormDto.CertificationDto cert : dto.getCertifications()) {
                if (cert.getName() != null && !cert.getName().isBlank()) {
                    resume.addCertification(new CertificationItem(
                            cert.getName().trim(),
                            cert.getIssuer(),
                            cert.getIssueYear(),
                            cert.getCredentialUrl(),
                            order++
                    ));
                }
            }
        }
    }

    public ResumeFormDto toFormDto(Resume resume) {
        ResumeFormDto dto = new ResumeFormDto();
        dto.setId(resume.getId());
        dto.setTitle(resume.getTitle());
        dto.setTemplate(resume.getTemplate());
        dto.setFullName(resume.getFullName());
        dto.setHeadline(resume.getHeadline());
        dto.setEmail(resume.getEmail());
        dto.setPhone(resume.getPhone());
        dto.setLocation(resume.getLocation());
        dto.setGithubUrl(resume.getGithubUrl());
        dto.setLinkedinUrl(resume.getLinkedinUrl());
        dto.setPortfolioUrl(resume.getPortfolioUrl());
        dto.setSummary(resume.getSummary());

        for (EducationItem e : resume.getEducationList()) {
            dto.getEducations().add(new ResumeFormDto.EducationDto(
                    e.getInstitution(), e.getDegree(), e.getFieldOfStudy(),
                    e.getStartDate(), e.getEndDate(), e.getGrade()
            ));
        }

        for (ExperienceItem ex : resume.getExperienceList()) {
            dto.getExperiences().add(new ResumeFormDto.ExperienceDto(
                    ex.getCompany(), ex.getRole(), ex.getLocation(),
                    ex.getStartDate(), ex.getEndDate(), ex.isCurrentRole(), ex.getBulletPoints()
            ));
        }

        for (ProjectItem p : resume.getProjectList()) {
            dto.getProjects().add(new ResumeFormDto.ProjectDto(
                    p.getTitle(), p.getTechnologies(), p.getDescription(), p.getProjectUrl()
            ));
        }

        for (SkillCategory s : resume.getSkillCategoryList()) {
            dto.getSkillCategories().add(new ResumeFormDto.SkillCategoryDto(
                    s.getCategoryName(), s.getSkillsCsv()
            ));
        }

        for (CertificationItem c : resume.getCertificationList()) {
            dto.getCertifications().add(new ResumeFormDto.CertificationDto(
                    c.getName(), c.getIssuer(), c.getIssueYear(), c.getCredentialUrl()
            ));
        }

        return dto;
    }
}
