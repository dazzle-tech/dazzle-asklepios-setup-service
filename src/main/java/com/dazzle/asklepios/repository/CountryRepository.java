package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Country;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    Page<Country> findByName(String name, Pageable pageable);

    Page<Country> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    Optional<Country> findByNameIgnoreCase(String name);

    Optional<Country> findByCodeIgnoreCase(String code);
}
