package com.portfolio.resume.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title; // Identifier for the user (e.g. "Software Engineer - Backend")

    @Enumerated(EnumType.STRING)
    @Column(name = "template_style", nullable = false, length = 40)
    private ResumeTemplate template = ResumeTemplate.MODERN_TECH;

    // Contact Details
    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(length = 150)
    private String headline; // e.g. "Full-Stack Java Engineer | Cloud & Distributed Systems"

    @Column(nullable = false, length = 120)
    private String email;

    @Column(length = 40)
    private String phone;

    @Column(length = 120)
    private String location; // e.g. "San Francisco, CA" or "Bangalore, India"

    @Column(length = 200)
    private String githubUrl;

    @Column(length = 200)
    private String linkedinUrl;

    @Column(length = 200)
    private String portfolioUrl;

    // Professional Summary
    @Column(length = 2500)
    private String summary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Child Collections (Using Sets with LinkedHashSet to prevent MultipleBagFetchException)
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private Set<EducationItem> educations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private Set<ExperienceItem> experiences = new LinkedHashSet<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private Set<ProjectItem> projects = new LinkedHashSet<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private Set<SkillCategory> skillCategories = new LinkedHashSet<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private Set<CertificationItem> certifications = new LinkedHashSet<>();

    public Resume() {
    }

    public Resume(String title, String fullName, String email) {
        this.title = title;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void addEducation(EducationItem item) {
        educations.add(item);
        item.setResume(this);
    }

    public void addExperience(ExperienceItem item) {
        experiences.add(item);
        item.setResume(this);
    }

    public void addProject(ProjectItem item) {
        projects.add(item);
        item.setResume(this);
    }

    public void addSkillCategory(SkillCategory item) {
        skillCategories.add(item);
        item.setResume(this);
    }

    public void addCertification(CertificationItem item) {
        certifications.add(item);
        item.setResume(this);
    }

    public List<EducationItem> getEducationList() {
        return new ArrayList<>(educations);
    }

    public List<ExperienceItem> getExperienceList() {
        return new ArrayList<>(experiences);
    }

    public List<ProjectItem> getProjectList() {
        return new ArrayList<>(projects);
    }

    public List<SkillCategory> getSkillCategoryList() {
        return new ArrayList<>(skillCategories);
    }

    public List<CertificationItem> getCertificationList() {
        return new ArrayList<>(certifications);
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ResumeTemplate getTemplate() {
        return template;
    }

    public void setTemplate(ResumeTemplate template) {
        this.template = template;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getPortfolioUrl() {
        return portfolioUrl;
    }

    public void setPortfolioUrl(String portfolioUrl) {
        this.portfolioUrl = portfolioUrl;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<EducationItem> getEducations() {
        return educations;
    }

    public void setEducations(Set<EducationItem> educations) {
        this.educations = educations;
    }

    public Set<ExperienceItem> getExperiences() {
        return experiences;
    }

    public void setExperiences(Set<ExperienceItem> experiences) {
        this.experiences = experiences;
    }

    public Set<ProjectItem> getProjects() {
        return projects;
    }

    public void setProjects(Set<ProjectItem> projects) {
        this.projects = projects;
    }

    public Set<SkillCategory> getSkillCategories() {
        return skillCategories;
    }

    public void setSkillCategories(Set<SkillCategory> skillCategories) {
        this.skillCategories = skillCategories;
    }

    public Set<CertificationItem> getCertifications() {
        return certifications;
    }

    public void setCertifications(Set<CertificationItem> certifications) {
        this.certifications = certifications;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Resume resume)) return false;
        if (id != null && resume.id != null) {
            return Objects.equals(id, resume.id);
        }
        return Objects.equals(title, resume.title) && Objects.equals(email, resume.email);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(title, email);
    }
}
