package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ActiveIngredientIndications;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActiveIngredientIndicationsRepository extends JpaRepository<ActiveIngredientIndications, Long> {
List<ActiveIngredientIndications> findByActiveIngredientId(Long activeIngredientId);
}
