package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceSetup, Long> {

    List<ServiceSetup> findByFacility_Id(Long facilityId);

    Page<ServiceSetup> findByFacility_Id(Long facilityId, Pageable pageable);

    Page<ServiceSetup> findByFacility_IdAndCategory(Long facilityId, ServiceCategory category, Pageable pageable);

    Page<ServiceSetup> findByFacility_IdAndNameContainingIgnoreCase(Long facilityId, String name, Pageable pageable);

    Page<ServiceSetup> findByFacility_IdAndCodeContainingIgnoreCase(Long facilityId, String code, Pageable pageable);

}
