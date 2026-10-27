package com.portfolio.survey.dto;

import java.util.ArrayList;
import java.util.List;

public class SurveyAnalyticsDto {
    private Long surveyId;
    private String title;
    private String slug;
    private long totalSubmissions;
    private List<QuestionStatsDto> questionStats = new ArrayList<>();

    public SurveyAnalyticsDto() {
    }

    public SurveyAnalyticsDto(Long surveyId, String title, String slug, long totalSubmissions) {
        this.surveyId = surveyId;
        this.title = title;
        this.slug = slug;
        this.totalSubmissions = totalSubmissions;
    }

    public Long getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(Long surveyId) {
        this.surveyId = surveyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public long getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(long totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public List<QuestionStatsDto> getQuestionStats() {
        return questionStats;
    }

    public void setQuestionStats(List<QuestionStatsDto> questionStats) {
        this.questionStats = questionStats;
    }
}
