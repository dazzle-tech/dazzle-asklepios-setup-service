package com.dazzle.asklepios.repository;


import com.dazzle.asklepios.domain.PrescriptionInstruction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionInstructionRepository extends JpaRepository<PrescriptionInstruction, Long> {
    Page<PrescriptionInstruction> findByCategory(String category, Pageable pageable);
    Page<PrescriptionInstruction> findByUnit(String unit, Pageable pageable);
    Page<PrescriptionInstruction> findByRout(String route, Pageable pageable);
    Page<PrescriptionInstruction> findByFrequency(String frequency, Pageable pageable);


}
