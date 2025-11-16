package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ActiveIngredientContraindications;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActiveIngredientContraindicationsRepository extends JpaRepository<ActiveIngredientContraindications, Long> {
    List<ActiveIngredientContraindications> findByActiveIngredientId(Long activeIngredientId);
}
