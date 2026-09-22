package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.ApprovalCoverageCompany;
import com.dazzle.asklepios.domain.enumeration.CoverageBasis;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;

import java.time.Instant;
import java.time.LocalDate;

public record CoverageContractResponseVM(
        Long id,
        GuarantorType guarantorType,
        Long companyId,
        String companyName,
        String companyCode,
        String code,
        String policyNumber,
        CoverageBasis coverageBasis,
        Long insurancePayerId,
        String insurancePayerName,
        Long priceListSetupId,
        String priceListName,
        LocalDate startDate,
        LocalDate endDate,
        Long parentPayerId,
        String parentPayerName,
        Long classId,
        String className,
        ApprovalCoverageCompany approvalCoverageCompany,
        Boolean isActive,
        String createdBy,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate
) {}
