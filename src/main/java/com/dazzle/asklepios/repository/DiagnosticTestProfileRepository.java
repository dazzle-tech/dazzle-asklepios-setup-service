package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosticTestProfileRepository extends JpaRepository<DiagnosticTestProfile, Long> {
    Page<DiagnosticTestProfile> findAllByTest_Id(Long testId, Pageable pageable);
    void deleteAllByTest_Id(Long testId);
}
