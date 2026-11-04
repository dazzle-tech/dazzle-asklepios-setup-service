package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.VaccineBrands;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccineBrandsRepository extends JpaRepository<VaccineBrands, Long> {

    Page<VaccineBrands> findByVaccine_Id(Long vaccineId, Pageable pageable);

    List<VaccineBrands> findByIdIn(List<Long> ids);

    @Query(
            "SELECT vb FROM VaccineBrands vb "
                    + "WHERE vb.isActive = true "
                    + "AND (LOWER(vb.name) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "OR LOWER(vb.code) LIKE LOWER(CONCAT('%', :search, '%')))"
    )
    Page<VaccineBrands> searchActive(@Param("search") String search, Pageable pageable);

}
