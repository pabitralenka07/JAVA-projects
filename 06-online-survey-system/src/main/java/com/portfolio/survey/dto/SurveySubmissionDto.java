package com.portfolio.survey.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates survey responses submitted by a user.
 */
public class SurveySubmissionDto {

    private Long surveyId;
    private String respondentId;

    // questionId -> single selected option ID (as String) or text answer
    private Map<Long, String> answers = new HashMap<>();

    // questionId -> list of selected option IDs for multi-choice questions
    private Map<Long, List<Long>> multiAnswers = new HashMap<>();

    public SurveySubmissionDto() {
    }

    public SurveySubmissionDto(Long surveyId) {
        this.surveyId = surveyId;
    }

    public Long getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(Long surveyId) {
        this.surveyId = surveyId;
    }

    public String getRespondentId() {
        return respondentId;
    }

    public void setRespondentId(String respondentId) {
        this.respondentId = respondentId;
    }

    public Map<Long, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Long, String> answers) {
        this.answers = answers;
    }

    public Map<Long, List<Long>> getMultiAnswers() {
        return multiAnswers;
    }

    public void setMultiAnswers(Map<Long, List<Long>> multiAnswers) {
        this.multiAnswers = multiAnswers;
    }
}
