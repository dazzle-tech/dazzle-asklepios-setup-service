package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentsRepository extends JpaRepository<Department, Long> {
    Page<Department> findByFacilityId(Long facilityId, Pageable pageable);
    Page<Department> findByDepartmentType(DepartmentType type, Pageable pageable);
    Page<Department> findByNameContainingIgnoreCase(String name, Pageable pageable);

}