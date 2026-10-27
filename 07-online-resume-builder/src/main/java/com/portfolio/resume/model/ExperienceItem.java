package com.portfolio.resume.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "resume_experiences")
public class ExperienceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String company;

    @Column(nullable = false, length = 120)
    private String role;

    @Column(length = 120)
    private String location;

    @Column(length = 50)
    private String startDate;

    @Column(length = 50)
    private String endDate;

    @Column(name = "is_current")
    private boolean currentRole;

    @Column(length = 3000)
    private String bulletPoints; // Newline separated points

    @Column(name = "sort_order")
    private int sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @JsonIgnore
    private Resume resume;

    public ExperienceItem() {
    }

    public ExperienceItem(String company, String role, String location,
                          String startDate, String endDate, boolean currentRole,
                          String bulletPoints, int sortOrder) {
        this.company = company;
        this.role = role;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.currentRole = currentRole;
        this.bulletPoints = bulletPoints;
        this.sortOrder = sortOrder;
    }

    public List<String> getBulletPointList() {
        if (bulletPoints == null || bulletPoints.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(bulletPoints.split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isCurrentRole() {
        return currentRole;
    }

    public void setCurrentRole(boolean currentRole) {
        this.currentRole = currentRole;
    }

    public String getBulletPoints() {
        return bulletPoints;
    }

    public void setBulletPoints(String bulletPoints) {
        this.bulletPoints = bulletPoints;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Resume getResume() {
        return resume;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExperienceItem that)) return false;
        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }
        return sortOrder == that.sortOrder && Objects.equals(company, that.company) && Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(company, role, sortOrder);
    }
}
