package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.OrganizationHoliday;
import com.dazzle.asklepios.domain.PolicyDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PolicyDefinitionRepository extends JpaRepository<PolicyDefinition, Long>, JpaSpecificationExecutor<OrganizationHoliday> {
}
