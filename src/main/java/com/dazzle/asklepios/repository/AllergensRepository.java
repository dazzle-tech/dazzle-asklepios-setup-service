package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Allergens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AllergensRepository extends JpaRepository<Allergens, Long> {
    String ALLERGENS = "allergens";

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);

}
