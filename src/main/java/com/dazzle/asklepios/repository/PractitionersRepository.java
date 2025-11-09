package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.domain.enumeration.Specialty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PractitionersRepository extends JpaRepository<Practitioner, Long> {

    Page<Practitioner> findAll(Pageable pageable);

    Page<Practitioner> findByFacilityId(Long facilityId, Pageable pageable);

    Page<Practitioner> findBySpecialty(Specialty specialty, Pageable pageable);
    Page<Practitioner> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName,
            String lastName,
            Pageable pageable
    );
    boolean existsByUserId(Long userId);

}
