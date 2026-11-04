package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.CoverageCopayment;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;

import java.math.BigDecimal;
import java.time.Instant;

public record CoverageCopaymentVM(
        Long id,
        Long facilityId,
        String facilityName,
        Boolean allDepartments,
        Long departmentId,
        String departmentName,
        EncounterType encounterType,
        InsuranceCoverageType valueType,
        BigDecimal valueAmount,
        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {
    public static CoverageCopaymentVM ofEntity(CoverageCopayment entity) {
        return ofEntity(entity, null, null);
    }

    public static CoverageCopaymentVM ofEntity(CoverageCopayment entity, String facilityName, String departmentName) {
        return new CoverageCopaymentVM(
                entity.getId(),
                entity.getFacilityId(),
                facilityName,
                entity.getAllDepartments(),
                entity.getDepartmentId(),
                departmentName,
                entity.getEncounterType(),
                entity.getValueType(),
                entity.getValueAmount(),
                entity.getIsActive(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate()
        );
    }
}
