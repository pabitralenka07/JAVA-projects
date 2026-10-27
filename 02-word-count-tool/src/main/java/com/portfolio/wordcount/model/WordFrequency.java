package com.portfolio.wordcount.model;

/**
 * Record representing word frequency occurrence and percentage distribution.
 */
public record WordFrequency(
        String word,
        long count,
        double percentage
) implements Comparable<WordFrequency> {

    @Override
    public int compareTo(WordFrequency other) {
        // Primary sort: descending by count; Secondary sort: ascending alphabetical by word
        int cmp = Long.compare(other.count, this.count);
        if (cmp != 0) {
            return cmp;
        }
        return this.word.compareToIgnoreCase(other.word);
    }
}
