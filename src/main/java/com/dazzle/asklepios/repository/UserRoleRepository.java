package com.dazzle.asklepios.repository;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.UserRole;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.UserRoleId> {
    List<UserRole> findByIdUserId(Long userId);
    List<UserRole> findByIdRoleId(Long roleId);

    @Query("""
        select distinct u
        from UserRole ur, Role r, User u
        where r.id = ur.id.roleId
          and u.id = ur.id.userId
          and r.facility.id = :facilityId
    """)
    List<User> findDistinctUsersByFacilityId(@Param("facilityId") Long facilityId);
}
