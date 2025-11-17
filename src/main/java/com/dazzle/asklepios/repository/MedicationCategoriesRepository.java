package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DuplicationCandidate;
import com.dazzle.asklepios.domain.MedicationCategories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationCategoriesRepository extends JpaRepository<MedicationCategories, Long> {
    List<MedicationCategories> findByNameContainingIgnoreCase(String name);
}
