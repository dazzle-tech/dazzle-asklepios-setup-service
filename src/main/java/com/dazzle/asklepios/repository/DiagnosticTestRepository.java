package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.enumeration.TestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DiagnosticTestRepository extends JpaRepository<DiagnosticTest, Long> {

    /**
     * Batch-load diagnostic tests with LOINC initialized for response mapping.
     * Used after paginated queries so page totals/filters stay accurate without join-fetch.
     */
    @EntityGraph(attributePaths = "loincCode")
    @Query("SELECT t FROM DiagnosticTest t WHERE t.id IN :ids")
    List<DiagnosticTest> findWithLoincByIdIn(@Param("ids") Collection<Long> ids);

    Page<DiagnosticTest> findByType(TestType type, Pageable pageable);

    List<DiagnosticTest> findAllByType(TestType type);

    Page<DiagnosticTest> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    Page<DiagnosticTest> findByInternalCodeContainingIgnoreCase(
            String internalCode,
            Pageable pageable
    );

    Page<DiagnosticTest> findByShortNameContainingIgnoreCase(
            String shortName,
            Pageable pageable
    );

    Page<DiagnosticTest> findByIsActive(Boolean isActive, Pageable pageable);

    Page<DiagnosticTest> findByIsActiveTrue(Pageable pageable);

    Page<DiagnosticTest> findByIsActiveTrueAndAppointableTrue(
            Pageable pageable
    );

    Page<DiagnosticTest> findByTypeAndNameContainingIgnoreCase(
            TestType type,
            String name,
            Pageable pageable
    );

    Page<DiagnosticTest> findByTypeAndIsActiveTrue(
            TestType type,
            Pageable pageable
    );

    Page<DiagnosticTest> findByTypeAndIsActiveTrueAndNameContainingIgnoreCase(
            TestType type,
            String name,
            Pageable pageable
    );

    /**
     * Active tests by type, optionally narrowed by catalog membership and/or
     * case-insensitive partial name match. Filters are applied before pagination.
     */
    @Query("""
            SELECT t FROM DiagnosticTest t
            WHERE t.type = :type
              AND t.isActive = true
              AND (:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:catalogId IS NULL OR EXISTS (
                    SELECT 1 FROM CatalogDiagnosticTest cdt
                    WHERE cdt.test = t AND cdt.catalog.id = :catalogId
                  ))
            """)
    Page<DiagnosticTest> searchActiveByType(
            @Param("type") TestType type,
            @Param("name") String name,
            @Param("catalogId") Long catalogId,
            Pageable pageable
    );
}
