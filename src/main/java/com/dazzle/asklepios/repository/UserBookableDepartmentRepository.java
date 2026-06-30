package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.UserBookableDepartment;
import com.dazzle.asklepios.domain.UserDepartment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBookableDepartmentRepository extends JpaRepository<UserBookableDepartment, Long> {

    List<UserBookableDepartment> findAllByUser_Id(Long userId);

    boolean existsByUserIdAndDepartmentId(Long userId, Long departmentId);

    List<UserBookableDepartment> findByUserId(Long userId);

    Optional<UserBookableDepartment> findByUserIdAndDepartmentId(Long userId, Long departmentId);

    List<UserBookableDepartment> findByUserIdAndDepartment_FacilityId(Long userId, Long facilityId);

    @EntityGraph(attributePaths = {"department", "department.facility"})
    Optional<UserBookableDepartment> findFirstByUserIdAndDepartment_Facility_Id(Long userId, Long facilityId);




}

