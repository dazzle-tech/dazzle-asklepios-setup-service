package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.SurveySection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveySectionRepository extends JpaRepository<SurveySection, Long>, JpaSpecificationExecutor<SurveySection> {

    // Sections can share an order: among them the oldest (lowest id) comes first
    Page<SurveySection> findAllByOrderByDisplayOrderAscIdAsc(Pageable pageable);

    Page<SurveySection> findByIsActiveOrderByDisplayOrderAscIdAsc(Boolean isActive, Pageable pageable);

    Page<SurveySection> findByNameEnContainingIgnoreCaseOrNameArContainingIgnoreCaseOrderByDisplayOrderAscIdAsc(
            String nameEn, String nameAr, Pageable pageable);
}
