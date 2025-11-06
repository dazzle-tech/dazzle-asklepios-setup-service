package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ActiveIngredientAdverseEffects;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActiveIngredientAdverseEffectsRepository extends JpaRepository<ActiveIngredientAdverseEffects, Long> {
    List<ActiveIngredientAdverseEffects> findByActiveIngredientId(Long activeIngredientId);
}