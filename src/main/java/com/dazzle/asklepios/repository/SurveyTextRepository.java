package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.SurveyText;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyTextRepository extends JpaRepository<SurveyText, Long> {

    boolean existsByTextKey(String textKey);

    Page<SurveyText> findByIsActive(Boolean isActive, Pageable pageable);

    Page<SurveyText> findByTextKeyContainingIgnoreCaseOrTextEnContainingIgnoreCaseOrTextArContainingIgnoreCase(
            String textKey, String textEn, String textAr, Pageable pageable);
}
