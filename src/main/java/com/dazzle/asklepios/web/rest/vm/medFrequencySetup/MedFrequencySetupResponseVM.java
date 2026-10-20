package com.dazzle.asklepios.web.rest.vm.medFrequencySetup;

import com.dazzle.asklepios.domain.MedFrequencySetup;
import com.dazzle.asklepios.domain.enumeration.FrequencyList;
import com.dazzle.asklepios.domain.enumeration.MedFrequency;

import java.time.Instant;

public record MedFrequencySetupResponseVM(
        Long id,
        MedFrequency frequency,
        Integer frequencyNumber,
        FrequencyList frequencyUnit,
        Boolean isActive,
        String createdBy,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate
) {
    public static MedFrequencySetupResponseVM ofEntity(MedFrequencySetup entity) {
        if (entity == null) return null;
        return new MedFrequencySetupResponseVM(
                entity.getId(),
                entity.getFrequency(),
                entity.getFrequencyNumber(),
                entity.getFrequencyUnit(),
                entity.getIsActive(),
                entity.getCreatedBy(),
                entity.getCreatedDate(),
                entity.getLastModifiedBy(),
                entity.getLastModifiedDate()
        );
    }
}
