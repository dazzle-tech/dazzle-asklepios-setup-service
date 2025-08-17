package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Facility;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {
    String FACILITIES = "facilities";

    boolean existsByNameIgnoreCase(String name);
}
