package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.enumeration.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findByGender(Gender gender);
    List<Patient> findByFirstNameContainingIgnoreCase(String firstName);
    List<Patient> findByLastNameContainingIgnoreCase(String lastName);
    List<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String firstName, String lastName, String email);
    Optional<Patient> findByEmail(String email);
    boolean existsByEmail(String email);

    List<Patient> findByDateOfBirth(LocalDate date);
    List<Patient> findByDateOfBirthBetween(LocalDate from, LocalDate to);

    Long id(Long id);
}
