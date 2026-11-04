package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceSetup, Long> {

    Page<ServiceSetup> findByCategory(ServiceCategory category, Pageable pageable);

    Page<ServiceSetup> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<ServiceSetup> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    Page<ServiceSetup> findByAbbreviationContainingIgnoreCase(String abbreviation, Pageable pageable);

    Page<ServiceSetup> findByPrice(BigDecimal price, Pageable pageable);

    Page<ServiceSetup> findByCurrency(Currency currency, Pageable pageable);

    Page<ServiceSetup> findByFacility_Id(Long facilityId, Pageable pageable);
    Page<ServiceSetup> findByIsActive(Boolean isActive, Pageable pageable);
    Page<ServiceSetup> findByIdIn(List<Long> ids, Pageable pageable);
    Page<ServiceSetup> findByIdInAndIsActiveTrue(List<Long> ids, Pageable pageable);

    Page<ServiceSetup> findByFacility_IdAndIsActiveTrue(Long facilityId, Pageable pageable);

    Page<ServiceSetup> findByIsActiveTrueAndAppointableTrueAndFacility_Id(Long facilityId, Pageable pageable);
}
