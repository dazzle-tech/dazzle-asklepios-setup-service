package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.UserDepartment;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDepartmentRepository extends JpaRepository<UserDepartment, Long> {
    Optional<UserDepartment> findByUserIdAndDepartmentId(Long userId, Long departmentId);
    List<UserDepartment> findByUserId(Long userId);
    @Modifying
    @Query("update UserDepartment ud set ud.isActive = ?2 where ud.department.id =?1")
    int updateActiveByDepartmentId( Long departmentId, boolean active);
}