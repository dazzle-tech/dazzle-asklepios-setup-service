package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Configuration;
import com.dazzle.asklepios.domain.enumeration.ConfigurationKeys;
import com.dazzle.asklepios.domain.enumeration.ConfigurationReferenceType;
import com.dazzle.asklepios.domain.enumeration.ConfigurationScope;
import com.dazzle.asklepios.domain.enumeration.ConfigurationValueType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {

    List<Configuration> findByKeyAndScopeTypeAndFacilityIsNull(ConfigurationKeys key, ConfigurationScope scopeType);

    List<Configuration> findByKeyAndScopeTypeAndFacility_Id(ConfigurationKeys key, ConfigurationScope scopeType, Long facilityId);

    List<Configuration> findByKeyAndScopeTypeAndFacilityIsNullAndIsActiveTrue(ConfigurationKeys key, ConfigurationScope scopeType);

    List<Configuration> findByKeyAndScopeTypeAndFacility_IdAndIsActiveTrue(
            ConfigurationKeys key,
            ConfigurationScope scopeType,
            Long facilityId
    );

    List<Configuration> findByKeyAndScopeTypeAndIsActiveTrueAndFacility_IdAndUsers_IdOrderByIdDesc(
            ConfigurationKeys key,
            ConfigurationScope scopeType,
            Long facilityId,
            Long userId
    );

    List<Configuration> findByKeyAndScopeTypeAndFacility_IdAndIdNotAndUsers_IdIn(
            ConfigurationKeys key,
            ConfigurationScope scopeType,
            Long facilityId,
            Long id,
            Collection<Long> userIds
    );

    Page<Configuration> findByValueContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String value, String description, Pageable pageable);

    Page<Configuration> findByValueContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrKey(
            String value,
            String description,
            ConfigurationKeys key,
            Pageable pageable
    );

    Page<Configuration> findAllByValueType(ConfigurationValueType valueType, Pageable pageable);

    Page<Configuration> findAllByReferenceType(ConfigurationReferenceType referenceType, Pageable pageable);

}
