package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.BillingConfiguration;
import com.dazzle.asklepios.domain.enumeration.BillingConfigurationKey;
import com.dazzle.asklepios.domain.enumeration.BillingConfigurationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillingConfigurationRepository
        extends JpaRepository<BillingConfiguration, Long> {

    Page<BillingConfiguration> findByFacilityId(
            Long facilityId,
            Pageable pageable
    );

    Page<BillingConfiguration> findByFacilityIdAndActiveTrue(
            Long facilityId,
            Pageable pageable
    );

    Page<BillingConfiguration> findByFacilityIdAndStatus(
            Long facilityId,
            BillingConfigurationStatus status,
            Pageable pageable
    );

    Optional<BillingConfiguration>
    findByFacilityIdAndConfigurationKey(
            Long facilityId,
            BillingConfigurationKey configurationKey
    );

    boolean existsByFacilityIdAndConfigurationKey(
            Long facilityId,
            BillingConfigurationKey configurationKey
    );

    boolean existsByFacilityIdAndConfigurationKeyAndIdNot(
            Long facilityId,
            BillingConfigurationKey configurationKey,
            Long id
    );
}