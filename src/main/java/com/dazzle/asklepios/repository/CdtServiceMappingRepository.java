package com.dazzle.asklepios.repository;
import com.dazzle.asklepios.domain.CdtServiceMapping;
import com.dazzle.asklepios.domain.ServiceSetup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface CdtServiceMappingRepository extends JpaRepository<CdtServiceMapping, Long> {

    List<CdtServiceMapping> findByCdtCode_Id(Long cdtId);

    @Query("select m.service.id from CdtServiceMapping m where m.cdtCode.id = ?1")
    List<Long> findServiceIdsByCdtId(Long cdtId);

    @Modifying
    @Query("""
           delete from CdtServiceMapping m
           where m.cdtCode.id = ?1
           """)
    void deleteAllForCdt(Long cdtId);
    @Modifying
    @Query("""
           delete from CdtServiceMapping m
           where m.cdtCode.id = ?1
             and m.service.id not in ?2
           """)
    void deleteExtrasForCdtNotIn(Long cdtId, Collection<Long> keepIds);

    @Query("select m.service from CdtServiceMapping m where m.cdtCode.id = ?1")
    List<ServiceSetup> findServicesByCdtId(Long cdtId);
}
