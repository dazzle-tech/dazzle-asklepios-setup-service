package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.FinancialDocumentSequence;
import com.dazzle.asklepios.domain.enumeration.biling.FinancialDocumentType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialDocumentSequenceRepository
        extends JpaRepository<FinancialDocumentSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            """
            SELECT s
            FROM FinancialDocumentSequence s
            WHERE s.facilityId = :facilityId
              AND s.documentType = :documentType
              AND s.periodKey = :periodKey
            """
    )
    Optional<FinancialDocumentSequence>
    findForUpdate(
            @Param("facilityId")
            Long facilityId,

            @Param("documentType")
            FinancialDocumentType documentType,

            @Param("periodKey")
            String periodKey
    );

    List<FinancialDocumentSequence>
    findByFacilityIdAndDocumentTypeOrderByPeriodKeyDesc(
            Long facilityId,
            FinancialDocumentType documentType
    );
}
