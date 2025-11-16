package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.BrandMedicationActiveIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandMedicationActiveIngredientRepository extends JpaRepository<BrandMedicationActiveIngredient, Long> {
    List<BrandMedicationActiveIngredient> findAllByBrandMedicationId(long brandMedicationId);
}
