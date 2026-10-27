package com.portfolio.survey.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An individual question's answer in a submitted survey response.
 */
@Entity
@Table(name = "response_answers")
public class ResponseAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    @JsonIgnore
    private SurveyResponse surveyResponse;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "selected_option_id")
    private Long selectedOptionId;

    @Column(name = "selected_option_ids", length = 500)
    private String selectedOptionIds; // Comma-separated option IDs for multi-choice

    @Column(name = "text_answer", length = 4000)
    private String textAnswer;

    public ResponseAnswer() {
    }

    public ResponseAnswer(Question question, Long selectedOptionId) {
        this.question = question;
        this.selectedOptionId = selectedOptionId;
    }

    public ResponseAnswer(Question question, List<Long> multiOptionIds) {
        this.question = question;
        if (multiOptionIds != null && !multiOptionIds.isEmpty()) {
            this.selectedOptionIds = multiOptionIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
    }

    public ResponseAnswer(Question question, String textAnswer) {
        this.question = question;
        this.textAnswer = textAnswer;
    }

    public List<Long> getMultiOptionIdList() {
        if (selectedOptionIds == null || selectedOptionIds.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(selectedOptionIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SurveyResponse getSurveyResponse() {
        return surveyResponse;
    }

    public void setSurveyResponse(SurveyResponse surveyResponse) {
        this.surveyResponse = surveyResponse;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Long selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public String getSelectedOptionIds() {
        return selectedOptionIds;
    }

    public void setSelectedOptionIds(String selectedOptionIds) {
        this.selectedOptionIds = selectedOptionIds;
    }

    public String getTextAnswer() {
        return textAnswer;
    }

    public void setTextAnswer(String textAnswer) {
        this.textAnswer = textAnswer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResponseAnswer that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
