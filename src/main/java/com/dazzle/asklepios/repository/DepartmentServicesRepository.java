package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DepartmentServices;
import com.dazzle.asklepios.domain.enumeration.patient.EncounterReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentServicesRepository extends JpaRepository<DepartmentServices, Long> {

    List<DepartmentServices> findByDepartment_Id(Long departmentId);

    boolean existsByDepartment_IdAndService(Long departmentId, EncounterReason service);

    void deleteByDepartment_Id(Long departmentId);
}