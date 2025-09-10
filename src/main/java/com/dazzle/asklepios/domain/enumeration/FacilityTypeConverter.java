package com.dazzle.asklepios.domain.enumeration;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FacilityTypeConverter implements AttributeConverter<FacilityType, String> {

    @Override
    public String convertToDatabaseColumn(FacilityType attribute) {
        return attribute == null ? null : attribute.name().toLowerCase();
    }

    @Override
    public FacilityType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : FacilityType.valueOf(dbData.toUpperCase()); // DB lowercase to enum UPPERCASE
    }
}
