package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiagnosticTestProfileRepository extends JpaRepository<DiagnosticTestProfile, Long> {
    Page<DiagnosticTestProfile> findAllByTest_Id(Long testId, Pageable pageable);

    void deleteAllByTest_Id(Long testId);

    Page<DiagnosticTestProfile> findAllByTest_IdAndIsDefaultFalse(Long testId, Pageable pageable);

    long countByTest_Id(Long testId);


    @Query("""
                update DiagnosticTestProfile p
                   set p.isDefault = false
                 where p.test.id = ?1
                   and p.id <> ?2
                   and p.isDefault = true
            """)
    int unsetDefaultsExcept(Long testId, Long keepId);

    Optional<DiagnosticTestProfile> findFirstByTest_IdAndIsDefaultTrue(Long testId);

}
