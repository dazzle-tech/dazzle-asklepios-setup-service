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
                from User u
                where exists (
                    select 1
                    from UserRole ur, Role r
                    where ur.id.userId = u.id
                      and r.id = ur.id.roleId
                      and r.facility.id = ?1
                )
            """)
    List<User> findDistinctUsersByFacilityId(Long facilityId);

}
