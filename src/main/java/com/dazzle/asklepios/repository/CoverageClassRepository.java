package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CoverageClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CoverageClassRepository extends JpaRepository<CoverageClass, Long> {

    Page<CoverageClass> findByCoverageContract_Id(Long contractId, Pageable pageable);

    Page<CoverageClass> findByCoverageContract_IdAndIsActive(Long contractId, Boolean isActive, Pageable pageable);

    List<CoverageClass> findByCoverageContract_IdIn(Collection<Long> contractIds);

    List<CoverageClass> findByCoverageContract_IdInAndIsActiveTrue(Collection<Long> contractIds);

    boolean existsByCoverageContract_IdAndNameIgnoreCase(Long contractId, String name);

    boolean existsByCoverageContract_IdAndNameIgnoreCaseAndIdNot(Long contractId, String name, Long id);
}
