package com.portfolio.wordcount.model;

/**
 * Immutable metrics record capturing comprehensive textual dimensions.
 */
public record TextStatistics(
        long wordCount,
        long charCountWithSpaces,
        long charCountWithoutSpaces,
        long lineCount,
        long sentenceCount,
        long paragraphCount,
        double estimatedReadingTimeMinutes
) {
    public static final double WORDS_PER_MINUTE = 200.0;

    public static TextStatistics empty() {
        return new TextStatistics(0, 0, 0, 0, 0, 0, 0.0);
    }

    /**
     * Merges two statistics records. Used when aggregating metrics across multiple files.
     */
    public TextStatistics combine(TextStatistics other) {
        if (other == null) return this;
        long totalWords = this.wordCount + other.wordCount;
        double readingTime = Math.round((totalWords / WORDS_PER_MINUTE) * 10.0) / 10.0;

        return new TextStatistics(
                totalWords,
                this.charCountWithSpaces + other.charCountWithSpaces,
                this.charCountWithoutSpaces + other.charCountWithoutSpaces,
                this.lineCount + other.lineCount,
                this.sentenceCount + other.sentenceCount,
                this.paragraphCount + other.paragraphCount,
                readingTime
        );
    }
}
