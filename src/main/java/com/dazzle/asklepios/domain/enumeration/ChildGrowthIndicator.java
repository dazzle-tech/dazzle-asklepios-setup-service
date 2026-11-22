package com.dazzle.asklepios.domain.enumeration;

public enum ChildGrowthIndicator {
    WFA("Weight-for-age"),
    LFA("Length-for-age"),
    HC("Head circumference"),
    WFL("Weight-for-length"),
    BMI("BMI-for-age");

    private final String label;

    ChildGrowthIndicator(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
