package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Screen;
import java.util.List;
import java.util.Optional;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {
    String SCREENS = "screens";
    @Query("SELECT sa.screen FROM ScreenAuthority sa WHERE sa.authorityName = :authorityName")
    List<Screen> findScreensByAuthorityName(@Param("authorityName") String authorityName);
    List<Screen> findByModuleId(Long moduleId);

    Optional<Screen> findByModuleIdAndName(Long moduleId, String name);

    boolean existsByModuleIdAndName(Long moduleId, String name);
}
