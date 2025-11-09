package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {
    String FACILITIES = "facilities";

    boolean existsByNameIgnoreCase(String name);
    @Query("SELECT f FROM Facility f WHERE f.ruleId IS NULL OR f.ruleId = ?1")
    List<Facility> findUnlinkedOrLinkedToRule( Long ruleId);


}
