package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CoverageContract;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoverageContractRepository
        extends JpaRepository<CoverageContract, Long>, JpaSpecificationExecutor<CoverageContract> {

    List<CoverageContract> findByInsurancePayerId(Long insurancePayerId);

    List<CoverageContract> findByInsurancePayerIdAndIsActiveTrue(Long insurancePayerId);

    List<CoverageContract> findByGuarantorTypeAndCompanyId(GuarantorType guarantorType, Long companyId);
}
