package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.FormEntries;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormEntriesRepository extends JpaRepository<FormEntries, Long> {

    Page<FormEntries> findByFacility_Id(Long facilityId, Pageable pageable);

    Page<FormEntries> findByDepartment_Id(Long departmentId, Pageable pageable);

    Page<FormEntries> findByTemplate_Id(Long templateId, Pageable pageable);

    Page<FormEntries> findByFacility_IdAndDepartment_Id(Long facilityId, Long departmentId, Pageable pageable);

    Page<FormEntries> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<FormEntries> findByTemplate_IdAndTitleContainingIgnoreCase(Long templateId, String title, Pageable pageable);

    Page<FormEntries> findByFacility_IdAndTitleContainingIgnoreCase(Long facilityId, String title, Pageable pageable);

    Page<FormEntries> findByDepartment_IdAndTitleContainingIgnoreCase(Long departmentId, String title, Pageable pageable);

    Page<FormEntries> findByFacility_IdAndDepartment_IdAndTitleContainingIgnoreCase(Long facilityId, Long departmentId, String title, Pageable pageable);

    Page<FormEntries> findByPatientId(Long patientId, Pageable pageable);

    Page<FormEntries> findByEncounterId(Long encounterId, Pageable pageable);
}
