package com.dazzle.asklepios.service.dto.icd10;

import java.io.Serializable;

public record ICDCategoryDTO(
        String categoryCode,
        String icdCoding,
        String categoryName,
        String categoryDescription,
        String parentCategoryCode
) implements Serializable { }
