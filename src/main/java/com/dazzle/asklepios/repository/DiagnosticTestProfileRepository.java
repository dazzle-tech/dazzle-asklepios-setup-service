package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosticTestProfileRepository extends JpaRepository<DiagnosticTestProfile, Long> {
    List<DiagnosticTestProfile> findAllByTest_Id(Long testId);
    void deleteAllByTest_Id(Long testId);
}
