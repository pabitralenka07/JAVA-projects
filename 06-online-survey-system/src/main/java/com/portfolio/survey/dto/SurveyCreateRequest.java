package com.portfolio.survey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class SurveyCreateRequest {

    @NotBlank(message = "Survey title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Size(max = 120, message = "Slug cannot exceed 120 characters")
    private String slug;

    private List<QuestionFormDto> questions = new ArrayList<>();

    public SurveyCreateRequest() {
    }

    public SurveyCreateRequest(String title, String description, String slug) {
        this.title = title;
        this.description = description;
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public List<QuestionFormDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionFormDto> questions) {
        this.questions = questions;
    }
}
