package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CdtDentalAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CdtDentalActionRepository extends JpaRepository<CdtDentalAction, Long> {
    List<CdtDentalAction> findByDentalAction_Id(Long dentalActionId);
    List<CdtDentalAction> findByCdtCode_Code(String cdtCode);
}

