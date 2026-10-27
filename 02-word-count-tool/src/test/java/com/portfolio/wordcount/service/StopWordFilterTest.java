package com.portfolio.wordcount.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StopWordFilterTest {

    @Test
    @DisplayName("Should correctly identify default English stop words regardless of casing")
    void testDefaultStopWords() {
        StopWordFilter filter = new StopWordFilter();

        assertThat(filter.isStopWord("the")).isTrue();
        assertThat(filter.isStopWord("THE")).isTrue();
        assertThat(filter.isStopWord("The")).isTrue();
        assertThat(filter.isStopWord("and")).isTrue();
        assertThat(filter.isStopWord("between")).isTrue();
        assertThat(filter.isStopWord("algorithm")).isFalse();
        assertThat(filter.isStopWord("architecture")).isFalse();
    }

    @Test
    @DisplayName("Should support custom stop word sets")
    void testCustomStopWords() {
        StopWordFilter custom = new StopWordFilter(Set.of("foo", "bar", "baz"));

        assertThat(custom.isStopWord("foo")).isTrue();
        assertThat(custom.isStopWord("bar")).isTrue();
        assertThat(custom.isStopWord("the")).isFalse(); // not in custom set
    }
}
