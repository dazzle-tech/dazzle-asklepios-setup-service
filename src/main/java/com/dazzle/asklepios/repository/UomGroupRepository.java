package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.UomGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UomGroupRepository extends JpaRepository<UomGroup, Long> {
    String UOMGROUP = "uomGroup";

    boolean existsByNameIgnoreCase(String name);
}
