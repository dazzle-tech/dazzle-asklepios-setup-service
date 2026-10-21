package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.InsuranceCompanyPolicy;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceCompanyPolicyRepository extends JpaRepository<InsuranceCompanyPolicy, Long> {

    @EntityGraph(attributePaths = "insuranceCompany")
    List<InsuranceCompanyPolicy> findByInsuranceCompany_IdOrderByValidFromDescIdDesc(Long insuranceCompanyId);

    boolean existsByIdAndInsuranceCompany_Id(Long id, Long insuranceCompanyId);

    boolean existsByInsuranceCompany_IdAndPolicyCodeIgnoreCase(Long insuranceCompanyId, String policyCode);

    boolean existsByInsuranceCompany_IdAndPolicyNumberIgnoreCase(Long insuranceCompanyId, String policyNumber);
}
