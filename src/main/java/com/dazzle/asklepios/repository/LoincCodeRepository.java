package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.LoincCode;
import com.dazzle.asklepios.domain.enumeration.LoincCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LoincCodeRepository extends JpaRepository<LoincCode, Long> {

    Optional<LoincCode> findByCode(String code);

    Page<LoincCode> findByCategory(LoincCategory category, Pageable pageable);

    Page<LoincCode> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    Page<LoincCode> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);
}
