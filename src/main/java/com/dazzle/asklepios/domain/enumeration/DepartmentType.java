package com.dazzle.asklepios.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum DepartmentType {
    INPATIENT_WARD,
    OUTPATIENT_CLINIC,
    REGISTRATION,
    EMERGENCY_ROOM,
    DAY_CASE,
    OPERATION_THEATER,
    LABORATORY,
    RADIOLOGY,
    PATHOLOGY,
    GENETIC_LAB,
    OPTIC_LAB,
    PHARMACY,
    BLOOD_BANK,
    DIALYSIS_ROOM,
    PHYSIOTHERAPY_ROOM,
    STERILIZATION_ROOM,
    MAINTENANCE,
    LAUNDRY,
    KITCHEN,
    MORGUE;

    @JsonCreator
    public static DepartmentType fromValue(String value) {
        if (value == null) {
            return null;
        }
        return DepartmentType.valueOf(
                value.toUpperCase().replace(" ", "_")
        );
    }

    @JsonValue
    public String toValue() {
        return Arrays.stream(name().split("_"))
                .map(word -> word.charAt(0) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
