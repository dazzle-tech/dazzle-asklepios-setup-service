package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.VaccineDoses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccineDosesRepository extends JpaRepository<VaccineDoses, Long> {


    Page<VaccineDoses> findByVaccine_Id(Long vaccineId, Pageable pageable);
    List<VaccineDoses> findByVaccine_Id(Long vaccineId);
}
