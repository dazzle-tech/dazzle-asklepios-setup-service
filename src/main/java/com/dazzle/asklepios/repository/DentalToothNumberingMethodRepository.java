package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DentalToothNumberingMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DentalToothNumberingMethodRepository extends JpaRepository<DentalToothNumberingMethod, Long>,
        JpaSpecificationExecutor<DentalToothNumberingMethod> {

    List<DentalToothNumberingMethod> findByIsActiveTrueOrderByNameAsc();

    Page<DentalToothNumberingMethod> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
