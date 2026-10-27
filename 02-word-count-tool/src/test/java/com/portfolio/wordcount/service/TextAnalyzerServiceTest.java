package com.portfolio.wordcount.service;

import com.portfolio.wordcount.model.AnalysisReport;
import com.portfolio.wordcount.model.TextStatistics;
import com.portfolio.wordcount.model.WordFrequency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TextAnalyzerServiceTest {

    private TextAnalyzerService analyzerService;

    @BeforeEach
    void setUp() {
        analyzerService = new TextAnalyzerService();
    }

    @Test
    @DisplayName("Should return empty statistics for null or blank input text")
    void testBlankAndNullText() {
        AnalysisReport nullReport = analyzerService.analyzeText("NullSource", null, 10, true);
        assertThat(nullReport.statistics().wordCount()).isEqualTo(0);
        assertThat(nullReport.statistics().charCountWithSpaces()).isEqualTo(0);
        assertThat(nullReport.topFrequencies()).isEmpty();

        AnalysisReport emptyReport = analyzerService.analyzeText("EmptySource", "   \n\t  ", 10, true);
        assertThat(emptyReport.statistics().wordCount()).isEqualTo(0);
        assertThat(emptyReport.statistics().lineCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should accurately count words, characters with spaces, and characters without spaces")
    void testWordAndCharacterCounts() {
        String input = "Java is powerful and simple."; // 5 words, 28 chars with spaces, 24 without
        AnalysisReport report = analyzerService.analyzeText("Sample", input, 10, false);
        TextStatistics stats = report.statistics();

        assertThat(stats.wordCount()).isEqualTo(5);
        assertThat(stats.charCountWithSpaces()).isEqualTo(28);
        assertThat(stats.charCountWithoutSpaces()).isEqualTo(24);
        assertThat(stats.sentenceCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should properly recognize contractions and hyphenated compound words as single words")
    void testContractionsAndHyphenatedWords() {
        String input = "It's a state-of-the-art solution that doesn't fail.";
        AnalysisReport report = analyzerService.analyzeText("ContractionSample", input, 10, false);

        assertThat(report.statistics().wordCount()).isEqualTo(7);
        List<String> words = report.topFrequencies().stream().map(WordFrequency::word).toList();
        assertThat(words).contains("it's", "state-of-the-art", "doesn't");
    }

    @Test
    @DisplayName("Should accurately detect sentence boundaries and paragraphs")
    void testSentencesAndParagraphs() {
        String input = """
                Hello world! How are you doing today?
                I hope you are doing great.

                This is a brand new paragraph. Are you ready?
                """;

        AnalysisReport report = analyzerService.analyzeText("StructureSample", input, 10, true);
        TextStatistics stats = report.statistics();

        // 5 sentences: "Hello world!", "How are you doing today?", "I hope you are doing great.", "This is a brand new paragraph.", "Are you ready?"
        assertThat(stats.sentenceCount()).isEqualTo(5);
        // 2 distinct paragraphs separated by blank lines
        assertThat(stats.paragraphCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should accurately accumulate word frequencies in HashMap and order descending")
    void testFrequencyRanking() {
        String input = "apple banana apple cherry apple banana date";
        AnalysisReport report = analyzerService.analyzeText("Fruits", input, 3, false);

        List<WordFrequency> topWords = report.topFrequencies();
        assertThat(topWords).hasSize(3);

        // apple: 3, banana: 2, cherry: 1 (or date: 1 alphabetical)
        assertThat(topWords.get(0).word()).isEqualTo("apple");
        assertThat(topWords.get(0).count()).isEqualTo(3);
        assertThat(topWords.get(1).word()).isEqualTo("banana");
        assertThat(topWords.get(1).count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should exclude common stop words when filterStopWords is enabled")
    void testStopWordFiltering() {
        String input = "the quick brown fox jumps over the lazy dog and the fox was happy";

        // Without stop words filtered
        AnalysisReport unfiltered = analyzerService.analyzeText("StopWordsOff", input, 5, false);
        assertThat(unfiltered.topFrequencies().get(0).word()).isEqualTo("the");
        assertThat(unfiltered.topFrequencies().get(0).count()).isEqualTo(3);

        // With stop words filtered
        AnalysisReport filtered = analyzerService.analyzeText("StopWordsOn", input, 5, true);
        List<String> topWords = filtered.topFrequencies().stream().map(WordFrequency::word).toList();
        assertThat(topWords).doesNotContain("the", "and", "was", "over");
        assertThat(topWords.get(0)).isEqualTo("fox");
        assertThat(filtered.filteredStopWordsCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should aggregate statistics across multiple files using Java NIO")
    void testMultiFileAggregation(@TempDir Path tempDir) throws IOException {
        Path file1 = tempDir.resolve("doc1.txt");
        Path file2 = tempDir.resolve("doc2.txt");

        Files.writeString(file1, "Java programming language is versatile.\n");
        Files.writeString(file2, "Java developers build scalable microservices.\n");

        AnalysisReport report = analyzerService.analyzeFiles(List.of(file1, file2), 5, true);
        TextStatistics stats = report.statistics();

        // file1: 5 words ("java", "programming", "language", "is", "versatile")
        // file2: 5 words ("java", "developers", "build", "scalable", "microservices")
        assertThat(stats.wordCount()).isEqualTo(10);
        assertThat(stats.lineCount()).isEqualTo(2);

        // "java" occurs in both files, total count = 2
        assertThat(report.topFrequencies().get(0).word()).isEqualTo("java");
        assertThat(report.topFrequencies().get(0).count()).isEqualTo(2);
    }
}
