package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Role;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByNameIgnoreCase(String name);

    List<Role> findByFacilityId(Long facilityId);
}