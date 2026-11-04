package com.dazzle.asklepios.domain.converter;

import com.dazzle.asklepios.domain.enumeration.MedDistributeArea;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stores a multi-select list of {@link MedDistributeArea} as comma-separated values
 * (e.g. "PHARMACY,HOSPITAL") in a varchar column.
 */
@Converter
public class MedDistributeAreaListConverter implements AttributeConverter<List<MedDistributeArea>, String> {

    @Override
    public String convertToDatabaseColumn(List<MedDistributeArea> areas) {
        if (areas == null || areas.isEmpty()) {
            return null;
        }
        return areas.stream()
                .distinct()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }

    @Override
    public List<MedDistributeArea> convertToEntityAttribute(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(MedDistributeArea::valueOf)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
