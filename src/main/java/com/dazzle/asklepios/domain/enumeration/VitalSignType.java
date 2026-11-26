package com.dazzle.asklepios.domain.enumeration;

import java.util.Arrays;
import java.util.Optional;

/**
 * Stable measurement keys used by the Vital Signs form, with their display unit.
 * Systolic and diastolic blood pressure stay separate.
 */
public enum VitalSignType {
    BLOOD_PRESSURE_SYSTOLIC("bloodPressureSystolic", "mmHg"),
    BLOOD_PRESSURE_DIASTOLIC("bloodPressureDiastolic", "mmHg"),
    HEART_RATE("heartRate", "bpm"),
    TEMPERATURE("temperature", "°C"),
    OXYGEN_SATURATION("oxygenSaturation", "%"),
    RESPIRATORY_RATE("respiratoryRate", "breaths/min");

    private final String key;
    private final String unit;

    VitalSignType(String key, String unit) {
        this.key = key;
        this.unit = unit;
    }

    public String getKey() {
        return key;
    }

    public String getUnit() {
        return unit;
    }

    public static Optional<VitalSignType> fromKey(String key) {
        return Arrays.stream(values()).filter(t -> t.key.equals(key)).findFirst();
    }
}
