package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.MedFrequencySetup;
import com.dazzle.asklepios.domain.enumeration.FrequencyList;
import com.dazzle.asklepios.domain.enumeration.MedFrequency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedFrequencySetupRepository extends JpaRepository<MedFrequencySetup, Long> {

    Optional<MedFrequencySetup> findByFrequency(MedFrequency frequency);

    boolean existsByFrequency(MedFrequency frequency);

    boolean existsByFrequencyAndIdNot(MedFrequency frequency, Long id);

    Page<MedFrequencySetup> findByIsActive(Boolean isActive, Pageable pageable);

    Page<MedFrequencySetup> findByFrequencyNumber(Integer frequencyNumber, Pageable pageable);

    Page<MedFrequencySetup> findByFrequencyUnit(FrequencyList frequencyUnit, Pageable pageable);
}
