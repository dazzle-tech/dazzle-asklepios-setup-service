package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.UserBookableDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBookableDepartmentRepository extends JpaRepository<UserBookableDepartment, Long> {

    List<UserBookableDepartment> findAllByUser_Id(Long userId);

    boolean existsByUserIdAndDepartmentId(Long userId, Long departmentId);

    Optional<UserBookableDepartment> findByUserIdAndDepartmentId(Long userId, Long departmentId);

    List<UserBookableDepartment> findByUserIdAndDepartment_FacilityId(Long userId, Long facilityId);

}

