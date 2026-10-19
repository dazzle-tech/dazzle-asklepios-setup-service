package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CoverageClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CoverageClassRepository extends JpaRepository<CoverageClass, Long> {

    @Override
    @EntityGraph(attributePaths = "insuranceCompanyPolicy")
    Optional<CoverageClass> findById(Long id);

    @EntityGraph(attributePaths = "insuranceCompanyPolicy")
    Page<CoverageClass> findByCoverageContract_Id(Long contractId, Pageable pageable);

    @EntityGraph(attributePaths = "insuranceCompanyPolicy")
    Page<CoverageClass> findByCoverageContract_IdAndIsActive(Long contractId, Boolean isActive, Pageable pageable);

    List<CoverageClass> findByCoverageContract_IdIn(Collection<Long> contractIds);

    List<CoverageClass> findByCoverageContract_IdInAndIsActiveTrue(Collection<Long> contractIds);

    @EntityGraph(attributePaths = {"coverageContract", "insuranceCompanyPolicy"})
    List<CoverageClass> findByIsActiveTrueAndCoverageContract_IsActiveTrueAndCoverageContract_InsurancePayerIdAndInsuranceCompanyPolicy_PolicyNumberIgnoreCase(
            Long insurancePayerId,
            String policyNumber
    );

    boolean existsByCoverageContract_IdAndNameIgnoreCase(Long contractId, String name);

    boolean existsByCoverageContract_IdAndNameIgnoreCaseAndIdNot(Long contractId, String name, Long id);
}
