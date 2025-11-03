package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DepartmentMedicalSheetsNurseVisbility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentMedicalSheetsNurseVisibilityRepository
        extends JpaRepository<DepartmentMedicalSheetsNurseVisbility, Long> {

    List<DepartmentMedicalSheetsNurseVisbility> findByDepartmentId(Long departmentId);

    @Modifying
    @Query(value = "DELETE FROM department_medical_sheets_nurse_visibility WHERE department_id = ?1", nativeQuery = true)
    void deleteByDepartmentId(Long departmentId);

}


