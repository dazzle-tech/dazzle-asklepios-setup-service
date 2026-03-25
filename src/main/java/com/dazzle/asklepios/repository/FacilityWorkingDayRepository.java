package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.FacilityWorkingDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacilityWorkingDayRepository extends JpaRepository<FacilityWorkingDay, Long> {
    List<FacilityWorkingDay> findAllByFacilityIdOrderByDayOfWeekAsc(Long facilityId);
    void deleteAllByFacilityId(Long facilityId);
}
