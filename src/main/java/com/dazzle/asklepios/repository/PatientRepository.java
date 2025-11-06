package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Page<Patient> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);

    Page<Patient> findByDateOfBirth(LocalDate date, Pageable pageable);

    Optional<Patient> findByEmail(String email);

    boolean existsByEmail(String email);
}
