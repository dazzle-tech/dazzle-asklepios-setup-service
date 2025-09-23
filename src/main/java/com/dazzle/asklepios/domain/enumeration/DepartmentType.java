package com.dazzle.asklepios.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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
    MORGUE ;

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static DepartmentType fromValue(String value) {
        return DepartmentType.valueOf(value.toUpperCase());
    }
}
