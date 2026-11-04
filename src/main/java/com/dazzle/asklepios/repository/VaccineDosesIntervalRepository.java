package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.VaccineDosesInterval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VaccineDosesIntervalRepository extends JpaRepository<VaccineDosesInterval, Long> {

    Page<VaccineDosesInterval> findByVaccine_Id(Long vaccineId, Pageable pageable);
    Optional<VaccineDosesInterval> findVaccineDosesIntervalByFromDose_Id(Long fromDoseId);
}
