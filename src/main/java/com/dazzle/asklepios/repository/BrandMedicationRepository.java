package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.BrandMedication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandMedicationRepository extends JpaRepository<BrandMedication, Long> {
    Page<BrandMedication> findByNameContainsIgnoreCase(String name, Pageable pageable);

    Page<BrandMedication> findByManufacturerContainsIgnoreCase(String manufacturer, Pageable pageable);

    Page<BrandMedication> findByDosageFormContainsIgnoreCase(String dosageForm, Pageable pageable);

    Page<BrandMedication> findByUsageInstructionsContainsIgnoreCase(String usageInstructions, Pageable pageable);

    Page<BrandMedication> findByRoaContainsIgnoreCase(String roa, Pageable pageable);

    Page<BrandMedication> findByExpiresAfterOpening(Boolean expiresAfter, Pageable pageable);

    Page<BrandMedication> findByUseSinglePatient(Boolean useSinglePatient, Pageable pageable);

    Page<BrandMedication> findByIsActive(Boolean isActive, Pageable pageable);

    @Query("""
              SELECT DISTINCT bai.brandMedication
            FROM BrandMedicationActiveIngredient bai
            WHERE bai.activeIngredients IN (
            SELECT bai2.activeIngredients
            FROM BrandMedicationActiveIngredient bai2
             WHERE bai2.brandMedication.id = :brandId
             )
            AND bai.brandMedication.id <> :brandId
                        """)
    List<BrandMedication> findAllBrandMedicationsSharingActiveIngredients(Long brandId);


}
