package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.security.SecurityUtils;
import com.dazzle.asklepios.service.PriceListItemDashboardService;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemDashboardVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/setup")
public class PriceListItemDashboardController {

    private static final Logger LOG = LoggerFactory.getLogger(PriceListItemDashboardController.class);

    private final PriceListItemDashboardService priceListItemDashboardService;

    public PriceListItemDashboardController(PriceListItemDashboardService priceListItemDashboardService) {
        this.priceListItemDashboardService = priceListItemDashboardService;
    }

    @GetMapping("/price-list-setups/item-dashboard")
    public ResponseEntity<PriceListItemDashboardVM> getItemDashboard() {
        Long facilityId = requireFacility();
        LOG.debug("REST get price list item dashboard overview facilityId={}", facilityId);
        return ResponseEntity.ok(priceListItemDashboardService.overview(facilityId));
    }

    @GetMapping("/price-list-setups/item-dashboard/items")
    public ResponseEntity<PriceListItemDashboardVM.ItemPageVM> searchDashboardItems(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PriceListItemType itemType,
            @ParameterObject Pageable pageable
    ) {
        Long facilityId = requireFacility();
        LOG.debug(
                "REST search price list item dashboard facilityId={} search={} itemType={} pageable={}",
                facilityId,
                search,
                itemType,
                pageable
        );
        return ResponseEntity.ok(
                priceListItemDashboardService.searchItems(facilityId, search, itemType, pageable)
        );
    }

    @GetMapping("/price-list-setups/item-dashboard/items/{itemType}/{sourceId}")
    public ResponseEntity<PriceListItemDashboardVM.ItemCardVM> getItemCoverage(
            @PathVariable PriceListItemType itemType,
            @PathVariable Long sourceId
    ) {
        Long facilityId = requireFacility();
        LOG.debug(
                "REST get price list item dashboard card facilityId={} itemType={} sourceId={}",
                facilityId,
                itemType,
                sourceId
        );
        return priceListItemDashboardService.findItem(facilityId, itemType, sourceId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private Long requireFacility() {
        return SecurityUtils.getCurrentUserFacility()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Missing mandatory claim 'tenant' in JWT."
                        )
                );
    }
}
