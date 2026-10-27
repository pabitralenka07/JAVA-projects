package com.portfolio.survey.dto;

public class OptionStatsDto {
    private Long optionId;
    private String optionText;
    private long count;
    private double percentage;

    public OptionStatsDto() {
    }

    public OptionStatsDto(Long optionId, String optionText, long count, double percentage) {
        this.optionId = optionId;
        this.optionText = optionText;
        this.count = count;
        this.percentage = percentage;
    }

    public Long getOptionId() {
        return optionId;
    }

    public void setOptionId(Long optionId) {
        this.optionId = optionId;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
