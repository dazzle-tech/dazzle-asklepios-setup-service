package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ActiveIngredientSynonyms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActiveIngredientSynonymsRepository extends JpaRepository<ActiveIngredientSynonyms, Long> {
    List<ActiveIngredientSynonyms> findByActiveIngredientId(Long activeIngredientId);
}
