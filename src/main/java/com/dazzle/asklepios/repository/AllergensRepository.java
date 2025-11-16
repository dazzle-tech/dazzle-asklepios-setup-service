package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Allergens;
import com.dazzle.asklepios.domain.enumeration.AllergenType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AllergensRepository extends JpaRepository<Allergens, Long> {
    Page<Allergens> findByType(AllergenType type, Pageable pageable);

    Page<Allergens> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
