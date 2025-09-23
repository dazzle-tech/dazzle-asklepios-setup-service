package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentsRepository extends JpaRepository<Department, Long> {
    List<Department> findByFacilityId(Long facilityId);

    List<Department> findByDepartmentType(DepartmentType departmentType);
    List<Department> findByNameContainingIgnoreCase (String name);
}