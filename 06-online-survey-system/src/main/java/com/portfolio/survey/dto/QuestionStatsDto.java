package com.portfolio.survey.dto;

import com.portfolio.survey.model.QuestionType;

import java.util.ArrayList;
import java.util.List;

public class QuestionStatsDto {
    private Long questionId;
    private String prompt;
    private QuestionType questionType;
    private long totalResponses;
    private List<OptionStatsDto> optionStats = new ArrayList<>();
    private List<String> textResponses = new ArrayList<>();

    public QuestionStatsDto() {
    }

    public QuestionStatsDto(Long questionId, String prompt, QuestionType questionType) {
        this.questionId = questionId;
        this.prompt = prompt;
        this.questionType = questionType;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public long getTotalResponses() {
        return totalResponses;
    }

    public void setTotalResponses(long totalResponses) {
        this.totalResponses = totalResponses;
    }

    public List<OptionStatsDto> getOptionStats() {
        return optionStats;
    }

    public void setOptionStats(List<OptionStatsDto> optionStats) {
        this.optionStats = optionStats;
    }

    public List<String> getTextResponses() {
        return textResponses;
    }

    public void setTextResponses(List<String> textResponses) {
        this.textResponses = textResponses;
    }
}
