package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.domain.BrandMedicationSubstitute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandMedicationSubstituteRepository extends JpaRepository<BrandMedicationSubstitute, Long> {
    @Query("SELECT DISTINCT b FROM BrandMedicationSubstitute b " +
            "WHERE b.brandMedication.id = :brandMedicationId OR b.alternativeBrandMedication.id = :brandMedicationId")
    List<BrandMedicationSubstitute> findAllByBrandOrAlternative(long brandMedicationId);

    @Modifying
    @Query("""
    DELETE FROM BrandMedicationSubstitute b
    WHERE (b.brandMedication.id = :brandId AND b.alternativeBrandMedication.id = :altBrandId)
       OR (b.brandMedication.id = :altBrandId AND b.alternativeBrandMedication.id = :brandId)
""")
    int deleteLinkBetween(long brandId, long altBrandId);

    @Query("""
    SELECT DISTINCT bms
    FROM BrandMedicationSubstitute bms
    WHERE bms.brandMedication.id = :brandMedicationId
       OR bms.alternativeBrandMedication.id = :brandMedicationId
    """)
    List<BrandMedicationSubstitute> findPairsByBrandOrAlternative(long brandMedicationId);

}
