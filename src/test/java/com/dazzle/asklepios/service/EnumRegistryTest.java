package com.dazzle.asklepios.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EnumRegistryTest {

    private EnumRegistry enumRegistry = new EnumRegistry();

    @Test
    void testGetAll_ShouldContainKnownEnums() {
        Map<String, List<String>> enums = enumRegistry.getAll();

        assertThat(enums).isNotEmpty();

        assertThat(enums).containsKey("Screen");
        assertThat(enums.get("Screen")).contains("SCHEDULING_SCREEN", "DEPARTMENTS");

        assertThat(enums).containsKey("Operation");
        assertThat(enums.get("Operation")).contains("VIEW", "EDIT");

        enums.values().forEach(list -> assertThat(list).isNotEmpty());
    }

    @Test
    void testGetAll_ValuesAreImmutable() {
        Map<String, List<String>> enums = enumRegistry.getAll();

        List<String> screenValues = enums.get("Screen");
        assertThat(screenValues).isNotNull();
        try {
            screenValues.add("NEW_SCREEN");
        } catch (UnsupportedOperationException e) {

        }
    }
}
