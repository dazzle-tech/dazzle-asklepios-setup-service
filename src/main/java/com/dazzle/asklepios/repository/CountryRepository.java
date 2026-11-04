package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Country;
import com.dazzle.asklepios.domain.enumeration.CountryName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    Page<Country> findByName(CountryName name, Pageable pageable);

    Page<Country> findByCode(String code, Pageable pageable);

    Page<Country> findByIsActiveTrue(Pageable pageable);

    @Query("""
        SELECT c
        FROM Country c
        WHERE c.isActive = true
          AND LOWER(CAST(c.name AS string)) LIKE LOWER(CONCAT(:name, '%'))
    """)
    Page<Country> searchActiveByNamePrefix(
            @Param("name") String name,
            Pageable pageable
    );
}