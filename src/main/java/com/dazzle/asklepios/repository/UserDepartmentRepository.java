package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.UserDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDepartmentRepository extends JpaRepository<UserDepartment, Long> {
    Optional<UserDepartment> findByUserIdAndDepartmentId(Long userId, Long departmentId);
    List<UserDepartment> findByUserId(Long userId);
}