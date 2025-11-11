package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.BrandMedicationSubstitute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandMedicationSubstituteRepository extends JpaRepository<BrandMedicationSubstitute, Long> {
    @Query("SELECT DISTINCT b FROM BrandMedicationSubstitute b " +
            "WHERE b.brandMedication.id = :brandMedicationId OR b.alternativeBrandMedication.id = :brandMedicationId")
    List<BrandMedicationSubstitute> findAllByBrandOrAlternative(long brandMedicationId);
}
