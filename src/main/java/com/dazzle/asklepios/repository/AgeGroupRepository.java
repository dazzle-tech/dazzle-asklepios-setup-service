package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.AgeGroup;
import com.dazzle.asklepios.domain.enumeration.AgeGroupType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AgeGroupRepository extends JpaRepository<AgeGroup, Long> {

    Page<AgeGroup> findByFacility_Id(Long facilityId, Pageable pageable);

    Page<AgeGroup> findByAgeGroup(AgeGroupType label, Pageable pageable);

    Page<AgeGroup> findByFromAge(BigDecimal fromAge, Pageable pageable);

    Page<AgeGroup> findByToAge(BigDecimal toAge, Pageable pageable);
     }
