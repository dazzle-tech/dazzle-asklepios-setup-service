package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PolicyDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PolicyDefinitionRepository extends JpaRepository<PolicyDefinition, Long>, JpaSpecificationExecutor<PolicyDefinition> {
    @Override
    @EntityGraph(attributePaths = "facility")
    Page<PolicyDefinition> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "facility")
    Optional<PolicyDefinition> findById(Long id);

    @EntityGraph(attributePaths = "facility")
    Page<PolicyDefinition> findByFacility_Id(Long facilityId, Pageable pageable);

    @EntityGraph(attributePaths = "facility")
    Page<PolicyDefinition> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    @EntityGraph(attributePaths = "facility")
    Page<PolicyDefinition> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @EntityGraph(attributePaths = "facility")
    Page<PolicyDefinition> findAllByIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = "facility")
    Page<PolicyDefinition> findByFacility_IdAndIsActiveTrue(Long facilityId, Pageable pageable);
}
