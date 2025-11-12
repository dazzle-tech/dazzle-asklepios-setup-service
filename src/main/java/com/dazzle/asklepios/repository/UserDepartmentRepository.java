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

    boolean existsByUserIdAndDepartmentId(Long userId, Long departmentId);

    boolean existsByUserIdAndIsDefaultTrueAndDepartment_Facility_Id(Long userId, Long facilityId);

    Optional<UserDepartment> findFirstByUserIdAndDepartment_Facility_IdAndIsDefaultTrue(Long userId, Long facilityId);

    Optional<UserDepartment> findFirstByUserIdAndDepartment_Facility_IdAndIsActiveTrueOrderByIdAsc(Long userId, Long facilityId);

    @Query("""
       SELECT ud FROM UserDepartment ud
        WHERE ud.user.id = :userId
          AND ud.department.facility.id = :facilityId
          AND ud.isActive = true
        ORDER BY ud.isDefault DESC, ud.id ASC
       """)
    List<UserDepartment> findByUserIdAndFacilityAndIsActiveTrueOrderByIsDefaultDesc(
            @Param("userId") Long userId,
            @Param("facilityId") Long facilityId);

    @Modifying
    @Query("""
           UPDATE UserDepartment ud
              SET ud.isDefault = false
            WHERE ud.user.id = :userId
              AND ud.isActive = true
              AND ud.isDefault = true
              AND ud.department.facility.id = :facilityId
           """)
    void clearDefaultForUserActiveInFacility(Long userId, Long facilityId);

}

