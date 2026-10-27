package com.portfolio.wordcount.service;

import com.portfolio.wordcount.model.AnalysisReport;
import com.portfolio.wordcount.model.TextStatistics;
import com.portfolio.wordcount.model.WordFrequency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Core text processing engine responsible for lexical analysis,
 * tokenization, frequency aggregation, and reading duration estimates.
 *
 * Algorithmic Complexity:
 * - Tokenization & Frequency Hashing: O(N) where N is text length.
 *   Utilizes HashMap for O(1) average lookup and increment.
 * - Top-N Word Ranking: O(U log U) where U is unique words count,
 *   leveraging Java Streams with custom comparator.
 */
public class TextAnalyzerService {

    // Matches valid words including contractions (e.g., "don't", "world-class")
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b[a-zA-Z0-9]+(?:['-][a-zA-Z0-9]+)*\\b");

    // Matches sentence boundary punctuation followed by whitespace or string end
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[.!?]+(?=\\s+|$)");

    private final StopWordFilter stopWordFilter;

    public TextAnalyzerService() {
        this.stopWordFilter = new StopWordFilter();
    }

    public TextAnalyzerService(StopWordFilter stopWordFilter) {
        this.stopWordFilter = stopWordFilter;
    }

    /**
     * Performs end-to-end lexical analysis on raw input text.
     */
    public AnalysisReport analyzeText(String sourceName, String text, int topN, boolean filterStopWords) {
        if (text == null || text.isBlank()) {
            return new AnalysisReport(sourceName, TextStatistics.empty(), Collections.emptyList(), 0, 0);
        }

        // 1. Character & whitespace counts
        long charCountWithSpaces = text.length();
        long charCountWithoutSpaces = text.chars().filter(ch -> !Character.isWhitespace(ch)).count();

        // 2. Line count
        long lineCount = text.lines().count();
        if (lineCount == 0 && !text.isEmpty()) {
            lineCount = 1;
        }

        // 3. Paragraph count: groups of non-blank lines separated by empty lines
        String[] paragraphs = text.split("(?m)^\\s*$");
        long paragraphCount = 0;
        for (String p : paragraphs) {
            if (!p.trim().isEmpty()) {
                paragraphCount++;
            }
        }
        if (paragraphCount == 0 && !text.isBlank()) {
            paragraphCount = 1;
        }

        // 4. Sentence count
        Matcher sentenceMatcher = SENTENCE_PATTERN.matcher(text);
        long sentenceCount = 0;
        while (sentenceMatcher.find()) {
            sentenceCount++;
        }
        if (sentenceCount == 0 && !text.isBlank()) {
            sentenceCount = 1;
        }

        // 5. Word tokenization and HashMap frequency accumulation
        Map<String, Long> frequencyMap = new HashMap<>();
        long totalWords = 0;
        long filteredStopWords = 0;

        Matcher wordMatcher = WORD_PATTERN.matcher(text);
        while (wordMatcher.find()) {
            String rawWord = wordMatcher.group().toLowerCase();
            totalWords++;

            if (filterStopWords && stopWordFilter.isStopWord(rawWord)) {
                filteredStopWords++;
                continue;
            }

            frequencyMap.merge(rawWord, 1L, Long::sum);
        }

        // 6. Estimated reading time (average 200 wpm)
        double readingTime = Math.round((totalWords / TextStatistics.WORDS_PER_MINUTE) * 10.0) / 10.0;

        TextStatistics stats = new TextStatistics(
                totalWords,
                charCountWithSpaces,
                charCountWithoutSpaces,
                lineCount,
                sentenceCount,
                paragraphCount,
                readingTime
        );

        // 7. Extract Top-N words sorted by frequency descending, then alphabetically ascending
        final long wordBaseForPercentage = totalWords > 0 ? totalWords : 1;
        List<WordFrequency> topWords = frequencyMap.entrySet().stream()
                .map(entry -> {
                    double pct = Math.round(((double) entry.getValue() / wordBaseForPercentage) * 1000.0) / 10.0;
                    return new WordFrequency(entry.getKey(), entry.getValue(), pct);
                })
                .sorted()
                .limit(topN > 0 ? topN : 10)
                .toList();

        return new AnalysisReport(
                sourceName != null ? sourceName : "Direct Input",
                stats,
                topWords,
                frequencyMap.size(),
                filteredStopWords
        );
    }

    /**
     * Analyzes a single file by path.
     */
    public AnalysisReport analyzeFile(Path path, int topN, boolean filterStopWords) throws IOException {
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File does not exist: " + path);
        }
        String content = Files.readString(path);
        return analyzeText(path.getFileName().toString(), content, topN, filterStopWords);
    }

    /**
     * Aggregates analysis across multiple files.
     */
    public AnalysisReport analyzeFiles(List<Path> paths, int topN, boolean filterStopWords) throws IOException {
        if (paths == null || paths.isEmpty()) {
            return new AnalysisReport("No Files", TextStatistics.empty(), Collections.emptyList(), 0, 0);
        }

        StringBuilder combinedText = new StringBuilder();
        List<String> fileNames = new ArrayList<>();

        for (int i = 0; i < paths.size(); i++) {
            Path path = paths.get(i);
            if (Files.exists(path)) {
                fileNames.add(path.getFileName().toString());
                String content = Files.readString(path).stripTrailing();
                if (!content.isEmpty()) {
                    if (!combinedText.isEmpty()) {
                        combinedText.append("\n");
                    }
                    combinedText.append(content);
                }
            }
        }

        String summarySource = "Batch (" + fileNames.size() + " files: " + String.join(", ", fileNames) + ")";
        return analyzeText(summarySource, combinedText.toString(), topN, filterStopWords);
    }
}
