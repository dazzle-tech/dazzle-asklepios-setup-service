package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ActiveIngredientSpecialPopulations;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActiveIngredientSpecialPopulationsRepository extends JpaRepository<ActiveIngredientSpecialPopulations, Long> {
    List<ActiveIngredientSpecialPopulations> findByActiveIngredientId(Long activeIngredientId);
}
