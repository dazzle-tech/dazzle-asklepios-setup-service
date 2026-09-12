package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CoveragePreApproval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoveragePreApprovalRepository extends JpaRepository<CoveragePreApproval, Long> {

    Page<CoveragePreApproval> findByCoverageContract_IdAndIsActive(
            Long contractId,
            Boolean isActive,
            Pageable pageable
    );

    Page<CoveragePreApproval> findByCoverageContract_Id(Long contractId, Pageable pageable);

    Page<CoveragePreApproval> findByTpaDefinition_IdAndIsActive(
            Long tpaId,
            Boolean isActive,
            Pageable pageable
    );

    Page<CoveragePreApproval> findByTpaDefinition_Id(Long tpaId, Pageable pageable);
}
