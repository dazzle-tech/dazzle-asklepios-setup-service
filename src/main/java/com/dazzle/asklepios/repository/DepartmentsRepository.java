package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentsRepository extends JpaRepository<Department, Long> {

    Page<Department> findByFacilityId(Long facilityId, Pageable pageable);

    Page<Department> findByType(DepartmentType type, Pageable pageable);

    Page<Department> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Department> findByFacilityIdAndIsActiveTrue(Long facilityId);

    Page<Department> findByTypeAndAppointableTrueAndIsActiveTrueAndFacilityId(
            DepartmentType type,
            Long facilityId,
            Pageable pageable
    );

    Page<Department> findByAppointableTrueAndIsActiveTrueAndFacilityId(
            Long facilityId,
            Pageable pageable
    );
    Page<Department> findByAppointableTrueAndIsActiveTrueAndType(
            DepartmentType type,
            Pageable pageable
    );
    Page<Department> findByTypeAndFacilityId(DepartmentType type, Long facilityId, Pageable pageable);

    Page<Department> findByTypeAndFacilityIdAndIsActiveTrue(DepartmentType type, Long facilityId, Pageable pageable);

    List<Department> findByIdIn(List<Long> ids);

    Page<Department> findByAppointableTrueAndIsActiveTrueAndFacilityIdAndEncounterType(
            Long facilityId,
            EncounterType encounterType,
            Pageable pageable
    );

    Page<Department> findByIsActiveTrue(Pageable pageable);
    List<Department> findByTypeAndFacilityIdAndIsActiveTrue(
            DepartmentType type,
            Long facilityId
    );
}
