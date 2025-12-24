package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Configuration;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.ConfigurationKeys;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {

    Optional<Configuration> findByKeyAndFacilityAndIsActiveTrue(ConfigurationKeys key, Facility facility);

    Optional<Configuration> findByKeyAndFacilityIsNullAndIsActiveTrue(ConfigurationKeys key);
}
