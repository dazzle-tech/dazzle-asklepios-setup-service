package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ICDDiagnosis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ICDDiagnosisRepository
        extends JpaRepository<ICDDiagnosis, Long>, JpaSpecificationExecutor<ICDDiagnosis> {

    Page<ICDDiagnosis> findByIcdCodingAndCategoryCode(String icdCoding, String categoryCode, Pageable pageable);

    Optional<ICDDiagnosis> findFirstByIcdCodeIgnoreCase(String icdCode);

    Optional<ICDDiagnosis> findByIcdDiagnosisUid(String icdDiagnosisUid);
}