package com.dazzle.asklepios.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChildGrowthBandTone {
    ACTION("action"),
    WATCH("watch"),
    NORMAL("normal");

    private final String value;

    ChildGrowthBandTone(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ChildGrowthBandTone fromValue(String raw) {
        if (raw == null) {
            return null;
        }
        for (ChildGrowthBandTone tone : values()) {
            if (tone.value.equalsIgnoreCase(raw) || tone.name().equalsIgnoreCase(raw)) {
                return tone;
            }
        }
        throw new IllegalArgumentException("Unknown status band tone: " + raw);
    }
}
