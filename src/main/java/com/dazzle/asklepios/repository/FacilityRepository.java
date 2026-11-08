package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Facility;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {
    String FACILITIES = "facilities";

    boolean existsByNameIgnoreCase(String name);
    @Query("SELECT f FROM Facility f WHERE f.ruleId IS NULL OR f.ruleId = ?1")
    List<Facility> findUnlinkedOrLinkedToRule( Long ruleId);


    List<Facility> findByIsActiveTrue();

    Page<Facility> findByIsActiveTrue(Pageable pageable);

    @Query("""
            SELECT f FROM Facility f
            WHERE f.isActive = true
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(f.code) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<Facility> searchActive(@Param("search") String search, Pageable pageable);
}
