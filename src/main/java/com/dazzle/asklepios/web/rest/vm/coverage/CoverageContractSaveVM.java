package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.ApprovalCoverageCompany;
import com.dazzle.asklepios.domain.enumeration.CoverageBasis;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record CoverageContractSaveVM(
        @NotNull
        GuarantorType guarantorType,

        @NotNull
        Long companyId,

        @NotBlank
        @Size(max = 50)
        String code,

        @NotBlank
        @Size(max = 100)
        String policyNumber,

        @NotNull
        CoverageBasis coverageBasis,

        Long insurancePayerId,

        @NotNull
        Long priceListSetupId,

        Long parentPayerId,

        ApprovalCoverageCompany approvalCoverageCompany,

        Boolean isActive
) implements Serializable {}
