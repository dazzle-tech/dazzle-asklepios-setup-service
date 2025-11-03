package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DepartmentMedicalSheetsVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentMedicalSheetsVisibilityRepository extends JpaRepository<DepartmentMedicalSheetsVisibility, Long> {

    List<DepartmentMedicalSheetsVisibility> findByDepartmentId(Long departmentId);

    @Modifying
    @Query(value = "DELETE FROM department_medical_sheets_visibility WHERE department_id = ?1", nativeQuery = true)
    void deleteByDepartmentId(Long departmentId);



}
