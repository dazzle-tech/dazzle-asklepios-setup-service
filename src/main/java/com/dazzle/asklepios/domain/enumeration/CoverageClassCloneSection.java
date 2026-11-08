package com.dazzle.asklepios.domain.enumeration;

/** A rule tab of a coverage class that can be copied when the class is cloned. */
public enum CoverageClassCloneSection {
    CO_PAYMENT(null),
    COVERAGE(CoverageTermType.COVERAGE),
    COVERAGE_LIMIT(CoverageTermType.LIMIT),
    CASH_LIMIT(CoverageTermType.CASH_LIMIT),
    DISCOUNT(null),
    EXCLUSION(null),
    PRE_APPROVAL(null);

    private final CoverageTermType termType;

    CoverageClassCloneSection(CoverageTermType termType) {
        this.termType = termType;
    }

    /** The coverage term type stored for this tab, or null when the tab is not a coverage term. */
    public CoverageTermType termType() {
        return termType;
    }
}
