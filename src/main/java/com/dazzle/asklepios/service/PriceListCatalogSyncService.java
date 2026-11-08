package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.domain.enumeration.TestType;
import com.dazzle.asklepios.repository.PriceListSetupItemRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PriceListCatalogSyncService {

    private static final Logger LOG =
            LoggerFactory.getLogger(PriceListCatalogSyncService.class);

    private final PriceListSetupItemRepository priceListSetupItemRepository;

    public void deactivateCatalogItem(
            PriceListItemType itemType,
            Long sourceId
    ) {
        if (itemType == null || sourceId == null) {
            return;
        }

        int updated = 0;
        var items = priceListSetupItemRepository.findAllByItemTypeAndSourceId(
                itemType,
                sourceId
        );
        for (var item : items) {
            if (Boolean.TRUE.equals(item.getIsActive())) {
                item.setIsActive(false);
                updated++;
            }
        }

        if (updated > 0) {
            priceListSetupItemRepository.saveAll(items);
            LOG.info(
                    "Deactivated {} price-list items for catalog type={} sourceId={}",
                    updated,
                    itemType,
                    sourceId
            );
        }
    }

    public void deactivateDiagnosticTest(Long sourceId, TestType testType) {
        if (sourceId == null) {
            return;
        }

        PriceListItemType itemType = mapTestType(testType);
        if (itemType != null) {
            deactivateCatalogItem(itemType, sourceId);
            return;
        }

        deactivateCatalogItem(PriceListItemType.LABORATORY, sourceId);
        deactivateCatalogItem(PriceListItemType.RADIOLOGY, sourceId);
        deactivateCatalogItem(PriceListItemType.PATHOLOGY, sourceId);
    }

    private PriceListItemType mapTestType(TestType testType) {
        if (testType == null) {
            return null;
        }

        return switch (testType) {
            case LABORATORY, MICROBIOLOGY -> PriceListItemType.LABORATORY;
            case RADIOLOGY -> PriceListItemType.RADIOLOGY;
            case PATHOLOGY -> PriceListItemType.PATHOLOGY;
        };
    }
}
