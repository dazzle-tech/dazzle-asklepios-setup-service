package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DischargePlanning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DischargePlanningRepository extends JpaRepository<DischargePlanning, Long> {

    Page<DischargePlanning> findByPatientId(Long patientId, Pageable pageable);

    Page<DischargePlanning> findByPatientIdAndEncounterId(Long patientId, Long encounterId, Pageable pageable);

    Optional<DischargePlanning> findByEncounterId(Long encounterId);

    boolean existsByEncounterId(Long encounterId);
}
