package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.WaseelSbsCatalog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}