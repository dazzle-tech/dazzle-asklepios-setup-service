package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CptCode;
import com.dazzle.asklepios.domain.enumeration.CptCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CptCodeRepository extends JpaRepository<CptCode, Long> {
    Optional<CptCode> findByCode(String code);
    Page<CptCode> findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String code, String description, Pageable pageable);
    Page<CptCode> findByCategory(CptCategory category, Pageable pageable);
    Page<CptCode> findByCodeContainingIgnoreCase(String code, Pageable pageable);
    Page<CptCode> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);
}
