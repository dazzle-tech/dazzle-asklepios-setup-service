package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Payor;
import com.dazzle.asklepios.domain.enumeration.biling.PayorCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayorRepository extends JpaRepository<Payor, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Page<Payor> findByIsActiveTrue(Pageable pageable);
    Page<Payor> findAll(Pageable pageable);
    Page<Payor> findByCategory(PayorCategory category, Pageable pageable);

    Page<Payor> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Payor> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    Page<Payor> findByCategoryAndNameContainingIgnoreCaseAndCodeContainingIgnoreCase(
            PayorCategory category, String name, String code, Pageable pageable
    );
}
