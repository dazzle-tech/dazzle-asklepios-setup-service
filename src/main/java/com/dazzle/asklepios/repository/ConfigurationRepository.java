package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Configuration;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.ConfigurationKeys;
import com.dazzle.asklepios.domain.enumeration.ConfigurationReferenceType;
import com.dazzle.asklepios.domain.enumeration.ConfigurationValueType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {

    Optional<Configuration> findByKeyAndFacilityAndIsActiveTrue(ConfigurationKeys key, Facility facility);

    Optional<Configuration> findByKeyAndFacilityIsNullAndIsActiveTrue(ConfigurationKeys key);

    Page<Configuration> findByValueContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String value, String description, Pageable pageable);

    @Query("""
                select c
                from Configuration c
                where
                    lower(c.value) like lower(concat('%', ?1, '%'))
                    or lower(c.description) like lower(concat('%', ?1, '%'))
                    or c.key = ?2
            """)
    Page<Configuration> searchValueDescriptionOrKey(
            String q,
            ConfigurationKeys key,
            Pageable pageable
    );

    Page<Configuration> findAllByValueType(ConfigurationValueType valueType, Pageable pageable);

    Page<Configuration> findAllByReferenceType(ConfigurationReferenceType referenceType, Pageable pageable);

}
