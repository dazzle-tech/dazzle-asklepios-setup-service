package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Facility;
import java.util.List;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {
    String FACILITIES = "facilities";

    boolean existsByNameIgnoreCase(String name);
    @Query("SELECT f FROM Facility f WHERE f.ruleId IS NULL OR f.ruleId = :roleId")
    List<Facility> findUnlinkedOrLinkedToRole(@Param("roleId") Long roleId);


}
