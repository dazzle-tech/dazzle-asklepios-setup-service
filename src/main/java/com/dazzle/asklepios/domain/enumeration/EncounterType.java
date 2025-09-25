package com.dazzle.asklepios.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum EncounterType {
    EMERGENCY,
    CLINIC,
    INPATIENT,
    DAYCASE;

    @JsonValue
    public String toValue() {
        return Arrays.stream(name().split("_"))
                .map(word -> word.charAt(0) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    @JsonCreator
    public static EncounterType fromValue(String value) {
        if (value == null) {
            return null;
        }
        return EncounterType.valueOf(
                value.toUpperCase().replace(" ", "_")
        );
    }
}
