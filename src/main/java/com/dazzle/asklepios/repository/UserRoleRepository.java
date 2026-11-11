package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.UserRoleId> {
    List<UserRole> findByIdUserId(Long userId);
    List<UserRole> findByIdRoleId(Long roleId);

    List<UserRole> findByRole_FacilityId(Long facilityId);

    @Query("""
            select distinct u
            from UserRole ur
            join ur.user u
            where ur.role.facilityId = :facilityId
            order by u.firstName asc, u.lastName asc, u.login asc
            """)
    List<User> findDistinctUsersByFacilityId(@Param("facilityId") Long facilityId);

    @Query("""
            select distinct u
            from UserRole ur
            join ur.user u
            where ur.role.facilityId = :facilityId
              and u.activated = true
            order by u.firstName asc, u.lastName asc, u.login asc
            """)
    List<User> findDistinctActiveUsersByFacilityId(@Param("facilityId") Long facilityId);

    List<UserRole> findByRole_FacilityIdAndUser_LoginContainingIgnoreCaseOrRole_FacilityIdAndUser_FirstNameContainingIgnoreCaseOrRole_FacilityIdAndUser_LastNameContainingIgnoreCaseOrRole_FacilityIdAndUser_EmailContainingIgnoreCase(
            Long facilityId,
            String login,
            Long sameFacilityForFirstName,
            String firstName,
            Long sameFacilityForLastName,
            String lastName,
            Long sameFacilityForEmail,
            String email
    );

}
