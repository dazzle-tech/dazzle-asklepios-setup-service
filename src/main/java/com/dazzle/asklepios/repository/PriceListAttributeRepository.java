package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PriceListAttribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceListAttributeRepository extends JpaRepository<PriceListAttribute, Long> {

    Page<PriceListAttribute> findByPriceListId(Long priceListId, Pageable pageable);

    Page<PriceListAttribute> findByPriceListIdAndIsActiveTrue(Long priceListId, Pageable pageable);

    Page<PriceListAttribute> findByIsActiveTrue(Pageable pageable);
}
