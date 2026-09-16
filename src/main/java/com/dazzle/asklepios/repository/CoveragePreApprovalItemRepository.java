package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CoveragePreApprovalItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoveragePreApprovalItemRepository extends JpaRepository<CoveragePreApprovalItem, Long> {

    Page<CoveragePreApprovalItem> findByPreApproval_IdAndIsActive(
            Long preApprovalId,
            Boolean isActive,
            Pageable pageable
    );

    Page<CoveragePreApprovalItem> findByPreApproval_Id(Long preApprovalId, Pageable pageable);

    List<CoveragePreApprovalItem> findByPreApproval_IdAndIsActiveTrue(Long preApprovalId);
}
