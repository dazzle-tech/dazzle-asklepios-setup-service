package com.dazzle.asklepios.repository;


import com.dazzle.asklepios.domain.PrescriptionInstruction;
import com.dazzle.asklepios.domain.enumeration.AgeGroupType;
import com.dazzle.asklepios.domain.enumeration.MedFrequency;
import com.dazzle.asklepios.domain.enumeration.MedRoa;
import com.dazzle.asklepios.domain.enumeration.UOM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionInstructionRepository extends JpaRepository<PrescriptionInstruction, Long> {
    Page<PrescriptionInstruction> findByCategory(AgeGroupType category, Pageable pageable);
    Page<PrescriptionInstruction> findByUnit(UOM unit, Pageable pageable);
    Page<PrescriptionInstruction> findByRout(MedRoa route, Pageable pageable);
    Page<PrescriptionInstruction> findByFrequency(MedFrequency frequency, Pageable pageable);


}
