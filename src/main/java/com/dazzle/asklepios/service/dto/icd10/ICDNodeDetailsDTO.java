package com.dazzle.asklepios.service.dto.icd10;

import java.io.Serializable;
import java.util.List;

public record ICDNodeDetailsDTO(
        ICDCategoryDTO selected,
        List<ICDCategoryDTO> children,
        List<ICDDiagnosisDTO> diagnoses
) implements Serializable { }
