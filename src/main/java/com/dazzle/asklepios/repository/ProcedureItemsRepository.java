package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ProcedureItems;
import com.dazzle.asklepios.domain.enumeration.ProcedureItemsType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcedureItemsRepository extends JpaRepository<ProcedureItems, Long> {

    Page<ProcedureItems> findByProcedure_Id(Long procedureId, Pageable pageable);

    List<ProcedureItems> findByTypeAndSourceIdAndIsActiveTrue(ProcedureItemsType type, Long sourceId);

    List<ProcedureItems> findByTypeAndSourceIdAndSpecialtyAndIsActiveTrue(
            ProcedureItemsType type,
            Long sourceId,
            String specialty
    );
}
