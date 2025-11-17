package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ActiveIngredientPreRequestedTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActiveIngredientPreRequestedTestRepository  extends JpaRepository<ActiveIngredientPreRequestedTest, Long> {
    List<ActiveIngredientPreRequestedTest> findByActiveIngredientId(Long activeIngredientId);
}
