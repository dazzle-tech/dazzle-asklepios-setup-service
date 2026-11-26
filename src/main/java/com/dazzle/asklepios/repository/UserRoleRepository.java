package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.UserRoleId> {
    List<UserRole> findByIdUserId(Long userId);
    List<UserRole> findByIdRoleId(Long roleId);

    List<UserRole> findByRole_FacilityId(Long facilityId);

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
