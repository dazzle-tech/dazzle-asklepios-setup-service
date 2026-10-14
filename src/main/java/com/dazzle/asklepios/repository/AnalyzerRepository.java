package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Analyzer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyzerRepository extends JpaRepository<Analyzer, Long> {
    Page<Analyzer> findByType(String type, Pageable pageable);

    Page<Analyzer> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    Page<Analyzer> findByIsActiveTrue(Pageable pageable);

    Page<Analyzer> findByFacility_Id(Long facilityId, Pageable pageable);
}
