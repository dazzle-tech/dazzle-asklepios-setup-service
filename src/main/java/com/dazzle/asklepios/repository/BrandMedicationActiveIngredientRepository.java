package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.BrandMedicationActiveIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface BrandMedicationActiveIngredientRepository extends JpaRepository<BrandMedicationActiveIngredient, Long> {
    List<BrandMedicationActiveIngredient> findAllByBrandMedicationId(long brandMedicationId);
    boolean existsByBrandMedicationId(Long brandMedicationId);

    @Query("""
        select distinct rel.brandMedication.id
        from BrandMedicationActiveIngredient rel
        where rel.activeIngredients.id in ?1
    """)
    List<Long> findDistinctBrandIdsByActiveIngredientIds(List<Long> activeIds);

    @Query("""
    select rel
    from BrandMedicationActiveIngredient rel
    join fetch rel.activeIngredients ai
    where rel.brandMedication.id in ?1
""")
    List<BrandMedicationActiveIngredient> findAllByBrandMedicationIdInWithActive(Collection<Long> brandIds);

    List<BrandMedicationActiveIngredient> findAllByBrandMedicationIdIn(List<Long> brandIds);
}
