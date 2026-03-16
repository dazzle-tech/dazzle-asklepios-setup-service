package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.OrganizationWorkingDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationWorkingDayRepository extends JpaRepository<OrganizationWorkingDay, Long> {
    List<OrganizationWorkingDay> findAllByOrganizationDefinitionIdOrderByDayOfWeekAsc(Long organizationDefinitionId);
    void deleteAllByOrganizationDefinitionId(Long organizationDefinitionId);
}
