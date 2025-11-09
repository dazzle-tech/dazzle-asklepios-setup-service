package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ProcedurePriceList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcedurePriceListRepository extends JpaRepository<ProcedurePriceList, Long> {
    Page<ProcedurePriceList> findByProcedureId(Long procedureId, Pageable pageable);
}
