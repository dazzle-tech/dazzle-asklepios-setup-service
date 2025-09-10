package com.dazzle.asklepios.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FacilityType {
    CLINIC,
    HOSPITAL;

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static FacilityType fromValue(String value) {
        return FacilityType.valueOf(value.toUpperCase());
    }
}
