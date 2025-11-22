package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CountryDistrict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryDistrictRepository extends JpaRepository<CountryDistrict, Long> {

    Page<CountryDistrict> findByCountry_Id(Long countryId, Pageable pageable);

    Page<CountryDistrict> findByCountry_IdAndNameContainingIgnoreCase(Long countryId, String name, Pageable pageable);

    Page<CountryDistrict> findByCountry_IdAndCodeContainingIgnoreCase(Long countryId, String code, Pageable pageable);
}
