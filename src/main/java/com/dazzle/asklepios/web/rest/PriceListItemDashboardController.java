package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.security.SecurityUtils;
import com.dazzle.asklepios.service.PriceListDataCheckService;
import com.dazzle.asklepios.service.PriceListItemDashboardService;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListDataCheckVM;
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
    private final PriceListDataCheckService priceListDataCheckService;

    public PriceListItemDashboardController(
            PriceListItemDashboardService priceListItemDashboardService,
            PriceListDataCheckService priceListDataCheckService
    ) {
        this.priceListItemDashboardService = priceListItemDashboardService;
        this.priceListDataCheckService = priceListDataCheckService;
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

    @GetMapping("/price-list-setups/item-dashboard/checks")
    public ResponseEntity<PriceListDataCheckVM.SummaryVM> getDataCheckSummary(
            @RequestParam(required = false) PriceListItemType itemType
    ) {
        Long facilityId = requireFacility();
        LOG.debug("REST get price list data check summary facilityId={} itemType={}", facilityId, itemType);
        return ResponseEntity.ok(priceListDataCheckService.summary(facilityId, itemType));
    }

    @GetMapping("/price-list-setups/item-dashboard/checks/{check}")
    public ResponseEntity<PriceListDataCheckVM.RowPageVM> getDataCheckRows(
            @PathVariable String check,
            @RequestParam(required = false) PriceListItemType itemType,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long facilityId = requireFacility();
        PriceListDataCheckService.Check selected = PriceListDataCheckService.Check.of(check)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown check: " + check));
        LOG.debug(
                "REST get price list data check rows facilityId={} check={} itemType={} search={} page={} size={}",
                facilityId,
                selected,
                itemType,
                search,
                page,
                size
        );
        return ResponseEntity.ok(
                priceListDataCheckService.rows(facilityId, selected, itemType, search, page, size)
        );
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
