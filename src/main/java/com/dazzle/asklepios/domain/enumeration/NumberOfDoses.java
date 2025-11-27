package com.dazzle.asklepios.domain.enumeration;

/**
 * Number of doses for a vaccine (1 to 10).
 */
public enum NumberOfDoses {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10);

    private final Integer value;

    NumberOfDoses(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
