package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CoverageContract;
import com.dazzle.asklepios.domain.enumeration.CoverageClassName;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoverageContractRepository extends JpaRepository<CoverageContract, Long> {

    boolean existsByGuarantorTypeAndCompanyIdAndCodeIgnoreCase(
            GuarantorType guarantorType,
            Long companyId,
            String code
    );

    boolean existsByGuarantorTypeAndCompanyIdAndCodeIgnoreCaseAndIdNot(
            GuarantorType guarantorType,
            Long companyId,
            String code,
            Long id
    );

    @Query("""
            SELECT c FROM CoverageContract c
            WHERE c.isActive = true
              AND c.insurancePayerId = :insurancePayerId
              AND LOWER(TRIM(c.policyNumber)) = LOWER(:policyNumber)
            """)
    List<CoverageContract> findActiveByInsurancePayerIdAndPolicyNumber(
            @Param("insurancePayerId") Long insurancePayerId,
            @Param("policyNumber") String policyNumber
    );

    @Query("""
            SELECT c FROM CoverageContract c
            WHERE (:guarantorType IS NULL OR c.guarantorType = :guarantorType)
              AND (:companyId IS NULL OR c.companyId = :companyId)
              AND (:insurancePayerId IS NULL OR c.insurancePayerId = :insurancePayerId)
              AND (:isActive IS NULL OR c.isActive = :isActive)
              AND (:className IS NULL OR c.className = :className)
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(c.policyNumber) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<CoverageContract> search(
            @Param("guarantorType") GuarantorType guarantorType,
            @Param("companyId") Long companyId,
            @Param("insurancePayerId") Long insurancePayerId,
            @Param("isActive") Boolean isActive,
            @Param("className") CoverageClassName className,
            @Param("search") String search,
            Pageable pageable
    );
}
