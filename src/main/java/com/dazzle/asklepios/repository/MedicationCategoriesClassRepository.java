package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.MedicationCategoriesClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicationCategoriesClassRepository extends JpaRepository<MedicationCategoriesClass, Long> {
}
