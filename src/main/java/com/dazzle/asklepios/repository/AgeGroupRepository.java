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

    List<AgeGroup> findByFacility_Id(Long facilityId);

    Page<AgeGroup> findByFacility_Id(Long facilityId, Pageable pageable);

    Page<AgeGroup> findByFacility_IdAndAgeGroup(Long facilityId, AgeGroupType ageGroup, Pageable pageable);

    Page<AgeGroup> findByFacility_IdAndFromAge(Long facilityId, BigDecimal fromAge, Pageable pageable);// أو:

    Page<AgeGroup> findByFacility_IdAndToAge(Long facilityId, BigDecimal toAge, Pageable pageable);}
