package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.SkillDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SkilDefinitionRepository extends JpaRepository<SkillDefinition, Long>, JpaSpecificationExecutor<SkillDefinition> {
    @Override
    @EntityGraph(attributePaths = "facility")
    Page<SkillDefinition> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "facility")
    Optional<SkillDefinition> findById(Long id);

    @EntityGraph(attributePaths = "facility")
    Page<SkillDefinition> findByFacility_Id(Long facilityId, Pageable pageable);

    @EntityGraph(attributePaths = "facility")
    Page<SkillDefinition> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    @EntityGraph(attributePaths = "facility")
    Page<SkillDefinition> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @EntityGraph(attributePaths = "facility")
    Page<SkillDefinition> findByTypeContainingIgnoreCase(String type, Pageable pageable);
}
