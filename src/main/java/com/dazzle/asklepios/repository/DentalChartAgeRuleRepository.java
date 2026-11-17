package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DentalChartAgeRule;
import com.dazzle.asklepios.domain.enumeration.DentalChartType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DentalChartAgeRuleRepository extends JpaRepository<DentalChartAgeRule, Long> {

    List<DentalChartAgeRule> findByIsActiveTrue();

    Page<DentalChartAgeRule> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<DentalChartAgeRule> findByChartType(DentalChartType chartType, Pageable pageable);
}
