package com.dazzle.asklepios.service.dto.icd10;

import java.io.Serializable;

public record ICDDiagnosisDTO(
        Long id,
        String icdDiagnosisUid,
        String icdCode,
        String icdCoding,
        String categoryCode,
        String icdShortDescription,
        String icdFullDescription
) implements Serializable { }
