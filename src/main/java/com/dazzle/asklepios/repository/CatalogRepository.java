package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Catalog;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.TestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CatalogRepository extends JpaRepository<Catalog, Long>, JpaSpecificationExecutor<Catalog> {
    Page<Catalog> findByDepartment_Id(Long departmentId, Pageable pageable);
    Page<Catalog> findByType(TestType type, Pageable pageable);
    Page<Catalog> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Catalog> findByDepartmentIdOrDepartmentIdIsNull(Long departmentId, Pageable pageable);
    Page<Catalog> findByAppointableTrueAndFacility_Id(Long facilityId, Pageable pageable);
}
