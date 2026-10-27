package com.portfolio.resume.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "resume_skills")
public class SkillCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String categoryName; // e.g. "Languages", "Frameworks & Tools", "Databases"

    @Column(nullable = false, length = 1000)
    private String skillsCsv; // e.g. "Java 17, Spring Boot, SQL, Docker"

    @Column(name = "sort_order")
    private int sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @JsonIgnore
    private Resume resume;

    public SkillCategory() {
    }

    public SkillCategory(String categoryName, String skillsCsv, int sortOrder) {
        this.categoryName = categoryName;
        this.skillsCsv = skillsCsv;
        this.sortOrder = sortOrder;
    }

    public List<String> getSkillList() {
        if (skillsCsv == null || skillsCsv.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(skillsCsv.split(","))
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSkillsCsv() {
        return skillsCsv;
    }

    public void setSkillsCsv(String skillsCsv) {
        this.skillsCsv = skillsCsv;
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
        if (!(o instanceof SkillCategory that)) return false;
        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }
        return sortOrder == that.sortOrder && Objects.equals(categoryName, that.categoryName);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(categoryName, sortOrder);
    }
}
