package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ChildGrowthSetup;
import com.dazzle.asklepios.domain.enumeration.ConfigurationScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChildGrowthSetupRepository extends JpaRepository<ChildGrowthSetup, Long> {

    Optional<ChildGrowthSetup> findByScopeTypeAndFacilityIsNullAndUserIsNull(ConfigurationScope scopeType);

    Optional<ChildGrowthSetup> findByScopeTypeAndFacility_IdAndUserIsNull(ConfigurationScope scopeType, Long facilityId);

    Optional<ChildGrowthSetup> findByScopeTypeAndFacility_IdAndUser_Id(
            ConfigurationScope scopeType,
            Long facilityId,
            Long userId
    );

    List<ChildGrowthSetup> findByScopeTypeAndFacility_IdAndUser_IdIn(
            ConfigurationScope scopeType,
            Long facilityId,
            Collection<Long> userIds
    );
}
