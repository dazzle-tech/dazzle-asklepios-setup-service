package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.InsuranceCompanyPolicy;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceCompanyPolicyRepository extends JpaRepository<InsuranceCompanyPolicy, Long>, JpaSpecificationExecutor<InsuranceCompanyPolicy> {

    @Override
    @EntityGraph(attributePaths = "insuranceCompany")
    List<InsuranceCompanyPolicy> findAll(Specification<InsuranceCompanyPolicy> specification, Sort sort);

    boolean existsByIdAndInsuranceCompany_Id(Long id, Long insuranceCompanyId);

    boolean existsByInsuranceCompany_IdAndPolicyCodeIgnoreCase(Long insuranceCompanyId, String policyCode);

    boolean existsByInsuranceCompany_IdAndPolicyNumberIgnoreCase(Long insuranceCompanyId, String policyNumber);

    boolean existsByInsuranceCompany_IdAndPolicyCodeIgnoreCaseAndIdNot(Long insuranceCompanyId, String policyCode, Long id);

    boolean existsByInsuranceCompany_IdAndPolicyNumberIgnoreCaseAndIdNot(Long insuranceCompanyId, String policyNumber, Long id);

    Optional<InsuranceCompanyPolicy> findByIdAndInsuranceCompany_Id(Long id, Long insuranceCompanyId);

    Optional<InsuranceCompanyPolicy> findFirstByInsuranceCompany_IdAndPolicyNumberIgnoreCase(
            Long insuranceCompanyId,
            String policyNumber
    );
}
