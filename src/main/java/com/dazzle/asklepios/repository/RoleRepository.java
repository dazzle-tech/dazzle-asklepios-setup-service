package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Role;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByNameIgnoreCase(String name);



    @EntityGraph(attributePaths = "facility")
    List<Role> findAll();

    @EntityGraph(attributePaths = "facility")
    Optional<Role> findById(Long id);

    @EntityGraph(attributePaths = "facility")
    List<Role> findByFacilityId(Long facilityId);

}