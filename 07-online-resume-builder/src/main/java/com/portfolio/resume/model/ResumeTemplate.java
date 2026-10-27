package com.portfolio.resume.model;

/**
 * Styling templates for resume generation.
 */
public enum ResumeTemplate {
    MODERN_TECH("Modern Tech (ATS Optimized)", "modern-tech"),
    EXECUTIVE("Executive Classic", "executive");

    private final String displayName;
    private final String templateKey;

    ResumeTemplate(String displayName, String templateKey) {
        this.displayName = displayName;
        this.templateKey = templateKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTemplateKey() {
        return templateKey;
    }
}
