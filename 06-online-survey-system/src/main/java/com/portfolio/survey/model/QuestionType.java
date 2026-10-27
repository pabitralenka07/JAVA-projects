package com.portfolio.survey.model;

/**
 * Supported question types within a survey.
 */
public enum QuestionType {
    SINGLE_CHOICE("Single Choice (Radio)", true),
    MULTIPLE_CHOICE("Multiple Choice (Checkbox)", true),
    TEXT("Open Text / Feedback", false);

    private final String displayName;
    private final boolean requiresOptions;

    QuestionType(String displayName, boolean requiresOptions) {
        this.displayName = displayName;
        this.requiresOptions = requiresOptions;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isRequiresOptions() {
        return requiresOptions;
    }
}
