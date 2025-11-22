package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Country;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    // Paged search by name (contains, case-insensitive)
    Page<Country> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Paged search by code (contains, case-insensitive)
    Page<Country> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    // Exact match by name (for validation/uniqueness checks if needed)
    Optional<Country> findByNameIgnoreCase(String name);

    // Exact match by code (for validation/uniqueness checks if needed)
    Optional<Country> findByCodeIgnoreCase(String code);
}
