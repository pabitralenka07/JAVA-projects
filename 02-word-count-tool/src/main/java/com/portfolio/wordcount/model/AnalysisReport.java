package com.portfolio.wordcount.model;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the complete analysis result for a single text or batch of files.
 */
public record AnalysisReport(
        String sourceName,
        TextStatistics statistics,
        List<WordFrequency> topFrequencies,
        int uniqueWordCount,
        long filteredStopWordsCount
) {
    public AnalysisReport {
        topFrequencies = topFrequencies == null ? Collections.emptyList() : Collections.unmodifiableList(topFrequencies);
    }
}
