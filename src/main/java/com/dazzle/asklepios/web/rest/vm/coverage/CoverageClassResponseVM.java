package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.CoverageClass;

import java.time.Instant;

public record CoverageClassResponseVM(
        Long id,
        Long coverageContractId,
        String name,
        Boolean isActive,
        String createdBy,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate

) {
    public static CoverageClassResponseVM of(CoverageClass coverageClass) {
        return new CoverageClassResponseVM(
                coverageClass.getId(),
                coverageClass.getCoverageContract() == null ? null : coverageClass.getCoverageContract().getId(),
                coverageClass.getName(),
                coverageClass.getIsActive(),
                coverageClass.getCreatedBy(),
                coverageClass.getCreatedDate(),
                coverageClass.getLastModifiedBy(),
                coverageClass.getLastModifiedDate()
        );
    }
}
