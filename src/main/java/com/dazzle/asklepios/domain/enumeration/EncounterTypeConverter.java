package com.dazzle.asklepios.domain.enumeration;

import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class EncounterTypeConverter  extends GeneralConverter<EncounterType> {

    public EncounterTypeConverter() {
        super(EncounterType.class);
    }
}
