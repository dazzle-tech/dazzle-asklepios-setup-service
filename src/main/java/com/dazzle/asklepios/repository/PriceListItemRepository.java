package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PriceListItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.EntityGraph;

public interface PriceListItemRepository extends JpaRepository<PriceListItem, Long> {

    @EntityGraph(attributePaths = {
            "service",
            "brandMedication",
            "diagnosticTest",
            "procedure"
    })
    Page<PriceListItem> findByIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {
            "service",
            "brandMedication",
            "diagnosticTest",
            "procedure"
    })
    Page<PriceListItem> findByPriceListId(Long priceListId, Pageable pageable);

    boolean existsByPriceListIdAndService_IdAndIdNot(Long priceListId, Long serviceId, Long id);

    boolean existsByPriceListIdAndBrandMedication_IdAndIdNot(Long priceListId, Long brandMedicationId, Long id);

    boolean existsByPriceListIdAndDiagnosticTest_IdAndIdNot(Long priceListId, Long diagnosticTestId, Long id);

    boolean existsByPriceListIdAndProcedure_IdAndIdNot(Long priceListId, Long procedureId, Long id);
}
