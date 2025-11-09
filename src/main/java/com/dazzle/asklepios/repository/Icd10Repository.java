package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Icd10Code;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
    public interface Icd10Repository extends JpaRepository<Icd10Code, Long> {
    Optional<Icd10Code> findByCode(String code);
    Page<Icd10Code> findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String code, String description, Pageable pageable);

}

