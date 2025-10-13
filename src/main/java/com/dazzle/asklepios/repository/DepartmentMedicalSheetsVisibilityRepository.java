package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DepartmentMedicalSheetsVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentMedicalSheetsVisibilityRepository extends JpaRepository<DepartmentMedicalSheetsVisibility, Long> {

    List<DepartmentMedicalSheetsVisibility> findByDepartmentId(Long departmentId);
}
