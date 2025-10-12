package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.UserFacilityDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserFacilityDepartmentRepository extends JpaRepository<UserFacilityDepartment, Long> {
    Optional<UserFacilityDepartment> findByUserIdAndDepartmentId(Long userId, Long departmentId);
    List<UserFacilityDepartment> findByUserId(Long userId);
}
