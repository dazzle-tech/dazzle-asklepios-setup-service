package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.web.rest.vm.profile.TestProfileCountVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiagnosticTestProfileRepository extends JpaRepository<DiagnosticTestProfile, Long> {

    Page<DiagnosticTestProfile> findAllByTest_Id(Long testId, Pageable pageable);

    void deleteAllByTest_Id(Long testId);

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

    List<DiagnosticTestProfile> findAllByTest_IdInAndIsDefaultTrue(Collection<Long> testIds);

    // -------------------------
    // Batch support (VM option)
    // -------------------------

    @Query("""
        select new com.dazzle.asklepios.web.rest.vm.profile.TestProfileCountVM(p.test.id, count(p))
          from DiagnosticTestProfile p
         where p.test.id in ?1
         group by p.test.id
    """)
    List<TestProfileCountVM> countByTestIds(Collection<Long> testIds);

    List<DiagnosticTestProfile> findAllByTest_IdInAndIsActiveTrue(Collection<Long> testIds);

    List<DiagnosticTestProfile> findAllByTest_IdInAndIsActiveTrueAndIsDefaultFalse(Collection<Long> testIds);


    List<DiagnosticTestProfile> findAllByTest_IdAndIsActiveTrue(Long testId);

    List<DiagnosticTestProfile> findAllByTest_IdAndIsActiveTrueAndIsDefaultFalse(Long testId);
}
