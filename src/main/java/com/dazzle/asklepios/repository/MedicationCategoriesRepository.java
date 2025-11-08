package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.MedicationCategories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicationCategoriesRepository extends JpaRepository<MedicationCategories, Long> {
}
