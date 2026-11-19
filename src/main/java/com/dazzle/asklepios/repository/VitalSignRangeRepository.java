package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.VitalSignRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VitalSignRangeRepository extends JpaRepository<VitalSignRange, Long> {

    List<VitalSignRange> findAllByOrderByVitalSignKeyAscAgeFromMonthsAsc();

    List<VitalSignRange> findByVitalSignKey(String vitalSignKey);
}
