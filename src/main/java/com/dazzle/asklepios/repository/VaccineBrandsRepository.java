package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.VaccineBrands;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VaccineBrandsRepository extends JpaRepository<VaccineBrands, Long> {

    Page<VaccineBrands> findByVaccine_Id(Long vaccineId, Pageable pageable);
}
