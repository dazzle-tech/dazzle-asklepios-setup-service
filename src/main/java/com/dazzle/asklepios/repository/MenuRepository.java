// src/main/java/com/dazzle/asklepios/repository/MenuRepository.java
package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Role; // <-- managed @Entity already in your app
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * IMPORTANT: bind the repository to a managed entity (Role).
 * The projection MenuRow is only the return type of the native query.
 */
public interface MenuRepository extends JpaRepository<Role, Long> {

    interface MenuRow {
        String getScreen();    // role_screen.screen
        String getOperation(); // role_screen.operation
    }

    @Query(value = """
        select rs.screen   as screen,
               rs.operation as operation
          from role_screen rs
          join role r       on r.id = rs.role_id
          join user_role ur on ur.role_id = r.id
         where ur.user_id = :userId
           and r.facility_id = :facilityId
        """, nativeQuery = true)
    List<MenuRow> findScreensForUserAndFacility(@Param("userId") Long userId,
                                                @Param("facilityId") Long facilityId);
}
