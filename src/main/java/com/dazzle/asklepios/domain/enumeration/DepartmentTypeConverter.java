package com.dazzle.asklepios.domain.enumeration;

import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class DepartmentTypeConverter extends GeneralConverter<DepartmentType> {

    public DepartmentTypeConverter() {
        super(DepartmentType.class);
    }
}
