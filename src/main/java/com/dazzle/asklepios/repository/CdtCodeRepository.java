package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CdtCode;
import com.dazzle.asklepios.domain.enumeration.CdtClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CdtCodeRepository extends JpaRepository<CdtCode, Long> {

    Optional<CdtCode> findByCode(String code);

    Page<CdtCode> findByCdtClass(CdtClass cdtClass, Pageable pageable);

    Page<CdtCode> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    Page<CdtCode> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    Page<CdtCode> findByIsActive(boolean isActive, Pageable pageable);
}
