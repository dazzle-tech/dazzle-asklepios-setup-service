package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ICDDiagnosis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICDDiagnosisRepository extends JpaRepository<ICDDiagnosis, Long> {

    Page<ICDDiagnosis> findByIcdCodingAndCategoryCode(String icdCoding, String categoryCode, Pageable pageable);

    Page<ICDDiagnosis>
    findByIcdCodeContainingIgnoreCaseOrIcdShortDescriptionContainingIgnoreCaseOrIcdFullDescriptionContainingIgnoreCaseOrIcdShortDescriptionOtherLanguageContainingIgnoreCaseOrIcdFullDescriptionOtherLanguageContainingIgnoreCase(
            String icdCode,
            String icdShortDescription,
            String icdFullDescription,
            String icdShortDescriptionOtherLanguage,
            String icdFullDescriptionOtherLanguage,
            Pageable pageable
    );

}
