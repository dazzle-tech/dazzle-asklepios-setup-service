package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ActiveIngredientDrugInteractions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActiveIngredientDrugInteractionsRepository extends JpaRepository<ActiveIngredientDrugInteractions, Long> {
    List<ActiveIngredientDrugInteractions> findByActiveIngredientId(Long activeIngredientId);
}