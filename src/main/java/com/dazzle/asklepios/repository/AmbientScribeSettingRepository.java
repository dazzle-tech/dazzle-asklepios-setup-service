package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.AmbientScribeSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmbientScribeSettingRepository extends JpaRepository<AmbientScribeSetting, Long> {
    Optional<AmbientScribeSetting> findAllByFacilityId(@Param("facilityId") Long facilityId);

    List<AmbientScribeSetting> findByFacilityIdIsNotNullAndAudioRetentionIsNotNull();
}
