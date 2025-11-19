package com.dazzle.asklepios.domain.enumeration;

/**
 * Enumeration representing the sequence number of a vaccine dose (1–10).
 */
public enum DoseNumber {
    FIRST(1),
    SECOND(2),
    THIRD(3),
    FOURTH(4),
    FIFTH(5),
    SIXTH(6),
    SEVENTH(7),
    EIGHTH(8),
    NINTH(9),
    TENTH(10);

    private final Integer order;

    DoseNumber(Integer order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
