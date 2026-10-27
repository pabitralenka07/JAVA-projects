package com.portfolio.survey.dto;

import com.portfolio.survey.model.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QuestionFormDto {

    @NotBlank(message = "Question prompt is required")
    private String prompt;

    @NotNull(message = "Question type is required")
    private QuestionType questionType = QuestionType.SINGLE_CHOICE;

    private boolean required = true;

    // Options can be supplied as newline-separated strings in UI textareas
    private String optionsRaw;

    private List<String> options = new ArrayList<>();

    public QuestionFormDto() {
    }

    public QuestionFormDto(String prompt, QuestionType questionType, boolean required, List<String> options) {
        this.prompt = prompt;
        this.questionType = questionType;
        this.required = required;
        this.options = options != null ? options : new ArrayList<>();
    }

    public List<String> resolveOptions() {
        if (optionsRaw != null && !optionsRaw.isBlank()) {
            return Arrays.stream(optionsRaw.split("\\r?\\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        return options != null ? options : List.of();
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

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getOptionsRaw() {
        return optionsRaw;
    }

    public void setOptionsRaw(String optionsRaw) {
        this.optionsRaw = optionsRaw;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }
}
