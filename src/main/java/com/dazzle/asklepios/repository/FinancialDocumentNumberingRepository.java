package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.FinancialDocumentNumbering;
import com.dazzle.asklepios.domain.enumeration.biling.FinancialDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialDocumentNumberingRepository
        extends JpaRepository<FinancialDocumentNumbering, Long> {

    List<FinancialDocumentNumbering>
    findByFacilityIdOrderByDocumentTypeAsc(
            Long facilityId
    );

    Optional<FinancialDocumentNumbering>
    findByFacilityIdAndDocumentType(
            Long facilityId,
            FinancialDocumentType documentType
    );

    boolean existsByFacilityIdAndDocumentType(
            Long facilityId,
            FinancialDocumentType documentType
    );

    boolean existsByFacilityIdAndDocumentTypeAndIdNot(
            Long facilityId,
            FinancialDocumentType documentType,
            Long id
    );
}
