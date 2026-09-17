package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceSetup, Long> {

    Page<ServiceSetup> findByCategory(ServiceCategory category, Pageable pageable);

    Page<ServiceSetup> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<ServiceSetup> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    Page<ServiceSetup> findByFacility_Id(Long facilityId, Pageable pageable);
    Page<ServiceSetup> findByIsActive(Boolean isActive, Pageable pageable);
    Page<ServiceSetup> findByIdIn(List<Long> ids, Pageable pageable);
    Page<ServiceSetup> findByIdInAndIsActiveTrue(List<Long> ids, Pageable pageable);

    Page<ServiceSetup> findByFacility_IdAndIsActiveTrue(Long facilityId, Pageable pageable);

    Page<ServiceSetup> findByIsActiveTrueAndAppointableTrueAndFacility_Id(Long facilityId, Pageable pageable);

    Optional<ServiceSetup> findFirstByFacility_IdAndCodeIgnoreCase(
            Long facilityId,
            String code
    );

    @Query("""
            SELECT s FROM ServiceSetup s
            WHERE s.isActive = true
              AND (:category IS NULL OR s.category = :category)
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<ServiceSetup> searchActive(
            @Param("search") String search,
            @Param("category") ServiceCategory category,
            Pageable pageable
    );
}
