package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.OrganizationHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface OrganizationHolidayRepository extends JpaRepository<OrganizationHoliday, Long>, JpaSpecificationExecutor<OrganizationHoliday> {
    List<OrganizationHoliday> findAllByOrderByStartDateDesc();

    List<OrganizationHoliday> findAllByIsActiveTrueOrderByStartDateDesc();
}
