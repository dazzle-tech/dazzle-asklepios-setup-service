package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.GuarantorType;

public record PolicyCoverageClassResponseVM(
        Long id,
        String name,
        Boolean isActive,
        Long coverageContractId,
        GuarantorType guarantorType,
        String coverageName
) {}
