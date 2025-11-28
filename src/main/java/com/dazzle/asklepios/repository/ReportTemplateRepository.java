package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ReportTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, Long> {

    Page<ReportTemplate> findByIsActiveTrue(Pageable pageable);

    Page<ReportTemplate> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
