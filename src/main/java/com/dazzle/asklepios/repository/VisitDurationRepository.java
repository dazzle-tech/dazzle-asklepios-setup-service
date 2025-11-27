package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.VisitDuration;
import com.dazzle.asklepios.domain.enumeration.VisitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitDurationRepository extends JpaRepository<VisitDuration, Long> {
    Page<VisitDuration> findByVisitType(VisitType visitType ,Pageable pageable);
}
