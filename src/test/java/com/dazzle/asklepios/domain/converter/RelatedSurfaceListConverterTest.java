package com.dazzle.asklepios.domain.converter;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RelatedSurfaceListConverterTest {

    private final RelatedSurfaceListConverter converter = new RelatedSurfaceListConverter();

    @Test
    void storesSurfacesAsCommaSeparatedValues() {
        assertThat(converter.convertToDatabaseColumn(List.of("MESIAL", "OCCLUSAL"))).isEqualTo("MESIAL,OCCLUSAL");
    }

    @Test
    void dropsBlankNullAndDuplicateValuesWhenStoring() {
        assertThat(converter.convertToDatabaseColumn(Arrays.asList("OCCLUSAL", " OCCLUSAL", null, " ")))
                .isEqualTo("OCCLUSAL");
    }

    @Test
    void storesNullForNullOrEmptyCollections() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToDatabaseColumn(List.of())).isNull();
    }

    @Test
    void readsLegacyCommaSeparatedValues() {
        assertThat(converter.convertToEntityAttribute("MESIAL, DISTAL,BUCCAL"))
                .containsExactly("MESIAL", "DISTAL", "BUCCAL");
    }

    @Test
    void readsSingleLegacyValueAsOneItemList() {
        assertThat(converter.convertToEntityAttribute("OCCLUSAL")).containsExactly("OCCLUSAL");
    }

    @Test
    void readsNullAndBlankAsEmptyList() {
        assertThat(converter.convertToEntityAttribute(null)).isEqualTo(new ArrayList<String>());
        assertThat(converter.convertToEntityAttribute("  ")).isEmpty();
    }
}
