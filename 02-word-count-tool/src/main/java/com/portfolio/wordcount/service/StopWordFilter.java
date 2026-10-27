package com.portfolio.wordcount.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Filter providing fast O(1) set-lookup for common English stop words.
 * Eliminating noise words allows true semantic keyword frequency analysis.
 */
public class StopWordFilter {

    private static final Set<String> DEFAULT_STOP_WORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by",
            "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't",
            "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have",
            "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself",
            "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into",
            "is", "isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my",
            "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our",
            "ours", "ourselves", "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's",
            "should", "shouldn't", "so", "some", "such", "than", "that", "that's", "the", "their", "theirs",
            "them", "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're",
            "they've", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "wasn't",
            "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's", "when", "when's",
            "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with", "won't",
            "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself",
            "yourselves"
    )));

    private final Set<String> activeStopWords;

    public StopWordFilter() {
        this.activeStopWords = new HashSet<>(DEFAULT_STOP_WORDS);
    }

    public StopWordFilter(Set<String> customStopWords) {
        this.activeStopWords = customStopWords == null ? new HashSet<>(DEFAULT_STOP_WORDS) : new HashSet<>(customStopWords);
    }

    public boolean isStopWord(String word) {
        if (word == null) return false;
        return activeStopWords.contains(word.trim().toLowerCase());
    }

    public int size() {
        return activeStopWords.size();
    }
}
