package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DuplicationCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface DuplicationCandidateRepository extends JpaRepository<DuplicationCandidate, Long> {


    @Query("SELECT MAX(dc.rule) FROM DuplicationCandidate dc")
    String findMaxRole();

    boolean existsById(Long id);

    List<DuplicationCandidate> findByRuleContaining(String text);

}
