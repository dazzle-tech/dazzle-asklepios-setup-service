package com.dazzle.asklepios.service.dto.icd10;

import com.dazzle.asklepios.domain.ICDCategory;
import com.dazzle.asklepios.domain.ICDDiagnosis;
import java.io.Serializable;
import java.util.List;

public record ICDNodeDetailsEntityDTO(
        ICDCategory selected,
        List<ICDCategory> children,
        List<ICDDiagnosis> diagnoses
) implements Serializable {}
