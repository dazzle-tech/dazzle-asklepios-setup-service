package com.dazzle.asklepios.domain.enumeration;

import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class FacilityTypeConverter  extends GeneralConverter<FacilityType> {

    public FacilityTypeConverter() {
        super(FacilityType.class);
    }
}
