package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Vaccine;
import com.dazzle.asklepios.domain.enumeration.RouteOfAdministration;
import com.dazzle.asklepios.domain.enumeration.VaccineType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VaccineRepository extends JpaRepository<Vaccine, Long> {

    Page<Vaccine> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Vaccine> findByRoa(RouteOfAdministration roa, Pageable pageable);

    Page<Vaccine> findByType(VaccineType type, Pageable pageable);

}
