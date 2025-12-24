package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.FormTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FormTemplateRepository extends JpaRepository<FormTemplate, Long>, JpaSpecificationExecutor<FormTemplate> {

    Page<FormTemplate> findByFacility_Id(Long facilityId, Pageable pageable);

    Page<FormTemplate> findByDepartment_Id(Long departmentId, Pageable pageable);

    Page<FormTemplate> findByFacility_IdAndDepartment_Id(Long facilityId, Long departmentId, Pageable pageable);

    Page<FormTemplate> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<FormTemplate> findByFacility_IdAndNameContainingIgnoreCase(Long facilityId, String name, Pageable pageable);

    Page<FormTemplate> findByDepartment_IdAndNameContainingIgnoreCase(Long departmentId, String name, Pageable pageable);

    Page<FormTemplate> findByFacility_IdAndDepartment_IdAndNameContainingIgnoreCase(Long facilityId, Long departmentId, String name, Pageable pageable);

}
