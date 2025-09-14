package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DuplicationCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DuplicationCandidateRepository extends JpaRepository<DuplicationCandidate, Long> {

    String CACHE_NAME = "duplicationCandidates";

    @Query("SELECT MAX(dc.role) FROM DuplicationCandidate dc")
    String findMaxRole();
}
