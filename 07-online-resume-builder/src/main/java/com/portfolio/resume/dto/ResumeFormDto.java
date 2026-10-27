package com.portfolio.resume.dto;

import com.portfolio.resume.model.ResumeTemplate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class ResumeFormDto {

    private Long id;

    @NotBlank(message = "Resume title is required")
    @Size(max = 150)
    private String title = "My Resume";

    private ResumeTemplate template = ResumeTemplate.MODERN_TECH;

    @NotBlank(message = "Full name is required")
    @Size(max = 120)
    private String fullName;

    @Size(max = 150)
    private String headline;

    @NotBlank(message = "Email address is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 120)
    private String email;

    @Size(max = 40)
    private String phone;

    @Size(max = 120)
    private String location;

    @Size(max = 200)
    private String githubUrl;

    @Size(max = 200)
    private String linkedinUrl;

    @Size(max = 200)
    private String portfolioUrl;

    @Size(max = 2500)
    private String summary;

    private List<EducationDto> educations = new ArrayList<>();
    private List<ExperienceDto> experiences = new ArrayList<>();
    private List<ProjectDto> projects = new ArrayList<>();
    private List<SkillCategoryDto> skillCategories = new ArrayList<>();
    private List<CertificationDto> certifications = new ArrayList<>();

    public ResumeFormDto() {
    }

    public static class EducationDto {
        private String institution;
        private String degree;
        private String fieldOfStudy;
        private String startDate;
        private String endDate;
        private String grade;

        public EducationDto() {}

        public EducationDto(String institution, String degree, String fieldOfStudy, String startDate, String endDate, String grade) {
            this.institution = institution;
            this.degree = degree;
            this.fieldOfStudy = fieldOfStudy;
            this.startDate = startDate;
            this.endDate = endDate;
            this.grade = grade;
        }

        public String getInstitution() { return institution; }
        public void setInstitution(String institution) { this.institution = institution; }
        public String getDegree() { return degree; }
        public void setDegree(String degree) { this.degree = degree; }
        public String getFieldOfStudy() { return fieldOfStudy; }
        public void setFieldOfStudy(String fieldOfStudy) { this.fieldOfStudy = fieldOfStudy; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
    }

    public static class ExperienceDto {
        private String company;
        private String role;
        private String location;
        private String startDate;
        private String endDate;
        private boolean currentRole;
        private String bulletPoints;

        public ExperienceDto() {}

        public ExperienceDto(String company, String role, String location, String startDate, String endDate, boolean currentRole, String bulletPoints) {
            this.company = company;
            this.role = role;
            this.location = location;
            this.startDate = startDate;
            this.endDate = endDate;
            this.currentRole = currentRole;
            this.bulletPoints = bulletPoints;
        }

        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public boolean isCurrentRole() { return currentRole; }
        public void setCurrentRole(boolean currentRole) { this.currentRole = currentRole; }
        public String getBulletPoints() { return bulletPoints; }
        public void setBulletPoints(String bulletPoints) { this.bulletPoints = bulletPoints; }
    }

    public static class ProjectDto {
        private String title;
        private String technologies;
        private String description;
        private String projectUrl;

        public ProjectDto() {}

        public ProjectDto(String title, String technologies, String description, String projectUrl) {
            this.title = title;
            this.technologies = technologies;
            this.description = description;
            this.projectUrl = projectUrl;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getTechnologies() { return technologies; }
        public void setTechnologies(String technologies) { this.technologies = technologies; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getProjectUrl() { return projectUrl; }
        public void setProjectUrl(String projectUrl) { this.projectUrl = projectUrl; }
    }

    public static class SkillCategoryDto {
        private String categoryName;
        private String skillsCsv;

        public SkillCategoryDto() {}

        public SkillCategoryDto(String categoryName, String skillsCsv) {
            this.categoryName = categoryName;
            this.skillsCsv = skillsCsv;
        }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getSkillsCsv() { return skillsCsv; }
        public void setSkillsCsv(String skillsCsv) { this.skillsCsv = skillsCsv; }
    }

    public static class CertificationDto {
        private String name;
        private String issuer;
        private String issueYear;
        private String credentialUrl;

        public CertificationDto() {}

        public CertificationDto(String name, String issuer, String issueYear, String credentialUrl) {
            this.name = name;
            this.issuer = issuer;
            this.issueYear = issueYear;
            this.credentialUrl = credentialUrl;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public String getIssueYear() { return issueYear; }
        public void setIssueYear(String issueYear) { this.issueYear = issueYear; }
        public String getCredentialUrl() { return credentialUrl; }
        public void setCredentialUrl(String credentialUrl) { this.credentialUrl = credentialUrl; }
    }

    // Getters and Setters for ResumeFormDto
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public ResumeTemplate getTemplate() { return template; }
    public void setTemplate(ResumeTemplate template) { this.template = template; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<EducationDto> getEducations() { return educations; }
    public void setEducations(List<EducationDto> educations) { this.educations = educations; }
    public List<ExperienceDto> getExperiences() { return experiences; }
    public void setExperiences(List<ExperienceDto> experiences) { this.experiences = experiences; }
    public List<ProjectDto> getProjects() { return projects; }
    public void setProjects(List<ProjectDto> projects) { this.projects = projects; }
    public List<SkillCategoryDto> getSkillCategories() { return skillCategories; }
    public void setSkillCategories(List<SkillCategoryDto> skillCategories) { this.skillCategories = skillCategories; }
    public List<CertificationDto> getCertifications() { return certifications; }
    public void setCertifications(List<CertificationDto> certifications) { this.certifications = certifications; }
}
