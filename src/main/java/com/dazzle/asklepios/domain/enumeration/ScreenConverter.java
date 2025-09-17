package com.dazzle.asklepios.domain.enumeration;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ScreenConverter implements AttributeConverter<Screen, String> {
    @Override
    public String convertToDatabaseColumn(Screen attribute) {
        if (attribute == null) {
            return null;
        }

        return attribute.name().toLowerCase();
    }

    @Override
    public Screen convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
   
        return Screen.valueOf(dbData.toUpperCase());
    }
}
