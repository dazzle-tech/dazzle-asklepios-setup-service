package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Service;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    Page<Service> findByFacility_Id(Long facilityId, Pageable pageable);
    Page<Service> findByFacility_IdAndCategory(Long facilityId, ServiceCategory category, Pageable pageable);
    Page<Service> findByFacility_IdAndNameContainingIgnoreCase(Long facilityId, String name, Pageable pageable);
    Page<Service> findByFacility_IdAndCodeContainingIgnoreCase(Long facilityId, String code, Pageable pageable);

    boolean existsByFacility_IdAndNameIgnoreCase(Long facilityId, String name);
}
