package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CdtServiceMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CdtServiceMappingRepository extends JpaRepository<CdtServiceMapping, Long> {
    List<CdtServiceMapping> findByCdtCode_Id(Long cdtId);
    void deleteAllByCdtCode_Id(Long cdtId);
    void deleteByCdtCode_IdAndService_IdNotIn(Long cdtId, Collection<Long> keepIds);
}
