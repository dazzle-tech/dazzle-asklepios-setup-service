package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ActiveIngredientFoodInteractions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActiveIngredientFoodInteractionsRepository extends JpaRepository<ActiveIngredientFoodInteractions, Long> {
    List<ActiveIngredientFoodInteractions> findByActiveIngredientId(Long activeIngredientId);

}
