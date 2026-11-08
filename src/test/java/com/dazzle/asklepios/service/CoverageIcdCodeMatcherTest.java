package com.dazzle.asklepios.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoverageIcdCodeMatcherTest {

    @Test
    void coversExactCodeAndDottedChildren() {
        assertThat(CoverageIcdCodeMatcher.covers("A00", "A00")).isTrue();
        assertThat(CoverageIcdCodeMatcher.covers("A00", "A00.0")).isTrue();
        assertThat(CoverageIcdCodeMatcher.covers("A00", "A00.1")).isTrue();
        assertThat(CoverageIcdCodeMatcher.covers("A00.0", "A00.00")).isTrue();
    }

    @Test
    void doesNotCoverSiblingOrUnrelatedCodes() {
        assertThat(CoverageIcdCodeMatcher.covers("A00", "A01")).isFalse();
        assertThat(CoverageIcdCodeMatcher.covers("A00.0", "A00.1")).isFalse();
        assertThat(CoverageIcdCodeMatcher.covers("A00", "B00")).isFalse();
        assertThat(CoverageIcdCodeMatcher.covers(null, "A00.0")).isFalse();
        assertThat(CoverageIcdCodeMatcher.covers("A00", " ")).isFalse();
    }

    @Test
    void coversEncounterWhenRuleMatchesCategory() {
        assertThat(CoverageIcdCodeMatcher.covers("A00", "A00.9", "A00")).isTrue();
        assertThat(CoverageIcdCodeMatcher.covers("A00.0", "A00.1", "A00")).isFalse();
    }
}
