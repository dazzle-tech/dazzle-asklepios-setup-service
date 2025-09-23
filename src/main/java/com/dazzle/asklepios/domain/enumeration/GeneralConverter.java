package com.dazzle.asklepios.domain.enumeration;

import jakarta.persistence.AttributeConverter;

public class GeneralConverter<T extends Enum<T>> implements AttributeConverter<T, String> {

    private final Class<T> enumType;

    public GeneralConverter(Class<T> enumType) {
        this.enumType = enumType;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        return attribute == null ? null : attribute.name().toLowerCase();
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Enum.valueOf(enumType, dbData.toUpperCase());
    }
}
