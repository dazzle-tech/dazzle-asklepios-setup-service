package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PriceList;
import com.dazzle.asklepios.domain.enumeration.biling.PriceListTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceListRepository extends JpaRepository<PriceList, Long> {

    Page<PriceList> findByIsActiveTrue(Pageable pageable);

    Page<PriceList> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<PriceList> findByType(PriceListTypes type, Pageable pageable);

    Page<PriceList> findByTypeAndNameContainingIgnoreCase(
            PriceListTypes type,
            String name,
            Pageable pageable
    );


    Page<PriceList> findByFacilityId(Long facilityId, Pageable pageable);

  
    Page<PriceList> findByFacilityIdIsNull(Pageable pageable);
}
