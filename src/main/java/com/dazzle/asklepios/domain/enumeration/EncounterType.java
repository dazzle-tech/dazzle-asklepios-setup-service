package com.dazzle.asklepios.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EncounterType {
    EMERGENCY,
    CLINIC,
    INPATIENT,
    DAYCASE ;

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static EncounterType fromValue(String value) {
        return EncounterType.valueOf(value.toUpperCase());
    }
}
