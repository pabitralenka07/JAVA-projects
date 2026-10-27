package com.portfolio.wordcount.cli;

import com.portfolio.wordcount.model.AnalysisReport;
import com.portfolio.wordcount.model.TextStatistics;
import com.portfolio.wordcount.model.WordFrequency;
import com.portfolio.wordcount.service.TextAnalyzerService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Command-Line Interface processor for Word Count & Text Analyzer.
 */
public class CliRunner {

    private final TextAnalyzerService analyzerService;

    public CliRunner() {
        this.analyzerService = new TextAnalyzerService();
    }

    public void run(String[] args) {
        List<Path> filePaths = new ArrayList<>();
        int topN = 10;
        boolean filterStopWords = true;
        boolean showHelp = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-f":
                case "--file":
                    while (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        filePaths.add(Paths.get(args[++i]));
                    }
                    break;
                case "-t":
                case "--top":
                    if (i + 1 < args.length) {
                        try {
                            topN = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid number for --top: " + args[i]);
                        }
                    }
                    break;
                case "--no-stop-words":
                    filterStopWords = false;
                    break;
                case "-h":
                case "--help":
                    showHelp = true;
                    break;
                default:
                    if (!args[i].equalsIgnoreCase("--cli") && !args[i].equalsIgnoreCase("--gui")) {
                        // Treat as direct file argument
                        filePaths.add(Paths.get(args[i]));
                    }
                    break;
            }
        }

        if (showHelp) {
            printHelp();
            return;
        }

        try {
            AnalysisReport report;
            if (!filePaths.isEmpty()) {
                if (filePaths.size() == 1) {
                    report = analyzerService.analyzeFile(filePaths.get(0), topN, filterStopWords);
                } else {
                    report = analyzerService.analyzeFiles(filePaths, topN, filterStopWords);
                }
            } else {
                System.out.println("Reading text from standard input (Ctrl+D to finish):");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                StringBuilder input = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    input.append(line).append("\n");
                }
                report = analyzerService.analyzeText("Standard Input", input.toString(), topN, filterStopWords);
            }

            printReport(report, filterStopWords);
        } catch (Exception e) {
            System.err.println("Error analyzing text: " + e.getMessage());
        }
    }

    public static void printReport(AnalysisReport report, boolean filteredStopWords) {
        TextStatistics s = report.statistics();

        System.out.println();
        System.out.println("===============================================================");
        System.out.println("               TEXT ANALYSIS REPORT : " + report.sourceName());
        System.out.println("===============================================================");
        System.out.printf("  Total Words          : %,d%n", s.wordCount());
        System.out.printf("  Characters (Total)   : %,d%n", s.charCountWithSpaces());
        System.out.printf("  Characters (No Space): %,d%n", s.charCountWithoutSpaces());
        System.out.printf("  Total Lines          : %,d%n", s.lineCount());
        System.out.printf("  Total Sentences      : %,d%n", s.sentenceCount());
        System.out.printf("  Paragraphs           : %,d%n", s.paragraphCount());
        System.out.printf("  Reading Time (est.)  : %.1f minutes (%d wpm)%n", s.estimatedReadingTimeMinutes(), (int) TextStatistics.WORDS_PER_MINUTE);
        System.out.printf("  Unique Vocabulary    : %,d words%n", report.uniqueWordCount());
        if (filteredStopWords) {
            System.out.printf("  Filtered Stop Words  : %,d (excluded from frequency ranking)%n", report.filteredStopWordsCount());
        }
        System.out.println("---------------------------------------------------------------");
        System.out.println("   RANK | FREQUENT WORD        | OCCURRENCES | DENSITY (%)    ");
        System.out.println("---------------------------------------------------------------");

        if (report.topFrequencies().isEmpty()) {
            System.out.println("   (No significant words found)");
        } else {
            int rank = 1;
            for (WordFrequency wf : report.topFrequencies()) {
                System.out.printf("   #%-3d | %-20s | %,11d | %6.1f%%%n", rank++, wf.word(), wf.count(), wf.percentage());
            }
        }
        System.out.println("===============================================================");
        System.out.println();
    }

    private void printHelp() {
        System.out.println("LexiCount — Word Count & Text Frequency Analyzer");
        System.out.println("Usage:");
        System.out.println("  mvn exec:java -Dexec.args=\"[options] [files...]\"");
        System.out.println("  java -jar 02-word-count-tool-1.0.0.jar [options] [files...]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -f, --file <path...>  Path(s) to text file(s) to analyze");
        System.out.println("  -t, --top <n>         Show top N frequent words (default: 10)");
        System.out.println("  --no-stop-words       Do not filter standard English stop words");
        System.out.println("  --gui                 Launch graphical interface");
        System.out.println("  -h, --help            Show this help reference");
    }
}
