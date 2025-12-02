package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Payor;
import com.dazzle.asklepios.domain.enumeration.biling.PayorCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayorRepository extends JpaRepository<Payor, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Page<Payor> findByIsActiveTrue(Pageable pageable);

    Page<Payor> findByCategoryAndIsActiveTrue(PayorCategory category, Pageable pageable);

    Page<Payor> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);

    Page<Payor> findByCodeContainingIgnoreCaseAndIsActiveTrue(String code, Pageable pageable);

    Page<Payor> findByCategoryAndNameContainingIgnoreCaseAndCodeContainingIgnoreCaseAndIsActiveTrue(
            PayorCategory category, String name, String code, Pageable pageable
    );
}
