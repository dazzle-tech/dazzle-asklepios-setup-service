package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PractitionerDepartment;
import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.domain.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PractitionerDepartmentRepository extends JpaRepository<PractitionerDepartment, Long> {

    Page<PractitionerDepartment> findAll(Pageable pageable);


    List<PractitionerDepartment> findByPractitionerId(Long practitionerId);

    List<PractitionerDepartment> findByDepartmentId(Long departmentId);

    boolean existsByPractitionerIdAndDepartmentId(Long practitionerId, Long departmentId);
}
