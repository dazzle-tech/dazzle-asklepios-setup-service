package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.WaseelSbsCatalog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WaseelSbsCatalogRepository extends JpaRepository<WaseelSbsCatalog, Long> {

    Optional<WaseelSbsCatalog> findBySbsCode(String sbsCode);

    boolean existsBySbsCode(String sbsCode);

    Page<WaseelSbsCatalog> findBySbsCodeContainingIgnoreCaseOrShortDescriptionContainingIgnoreCaseOrLongDescriptionContainingIgnoreCase(
            String sbsCode,
            String shortDescription,
            String longDescription,
            Pageable pageable
    );

    @Query("""
            SELECT c
            FROM WaseelSbsCatalog c
            WHERE (:activeOnly = false OR c.isActive = true)
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(c.sbsCode) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(c.shortDescription) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(c.longDescription) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(c.waseelItemType) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<WaseelSbsCatalog> searchCatalog(
            @Param("search") String search,
            @Param("activeOnly") boolean activeOnly,
            Pageable pageable
    );
}