package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.PriceListItem;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.service.PriceListItemService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemResponseVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemSaveVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class PriceListItemController {

    private static final Logger LOG = LoggerFactory.getLogger(PriceListItemController.class);

    private final PriceListItemService priceListItemService;

    public PriceListItemController(PriceListItemService priceListItemService) {
        this.priceListItemService = priceListItemService;
    }

    @PostMapping("/price-list-items")
    public ResponseEntity<PriceListItemResponseVM> create(
            @Valid @RequestBody PriceListItemSaveVM vm
    ) {
        LOG.debug("REST request to create PriceListItem : {}", vm);

        validateItemTargetByType(
                vm.itemType(),
                vm.serviceId(),
                vm.brandMedicationId(),
                vm.diagnosticTestId(),
                vm.procedureId()
        );

        PriceListItem saved = priceListItemService.create(vm);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        LOG.debug("Created PriceListItem id={}", saved.getId());

        return ResponseEntity
                .created(location)
                .body(PriceListItemResponseVM.ofEntity(saved));
    }

    @PutMapping("/price-list-items")
    public ResponseEntity<PriceListItemResponseVM> update(
            @Valid @RequestBody PriceListItemUpdateVM vm
    ) {
        LOG.debug("REST request to update PriceListItem : {}", vm);

        validateItemTargetByType(
                vm.itemType(),
                vm.serviceId(),
                vm.brandMedicationId(),
                vm.diagnosticTestId(),
                vm.procedureId()
        );

        PriceListItem updated = priceListItemService.update(vm);

        LOG.debug("Updated PriceListItem id={}", updated.getId());

        return ResponseEntity.ok(PriceListItemResponseVM.ofEntity(updated));
    }

    @GetMapping("/price-list-items")
    public ResponseEntity<List<PriceListItem>> findAll(
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get PriceListItems page={}", pageable);

        Page<PriceListItem> page = priceListItemService.findAll(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/price-list-items/active")
    public ResponseEntity<List<PriceListItem>> findAllActive(
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get active PriceListItems page={}", pageable);

        Page<PriceListItem> page = priceListItemService.findAllActive(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/price-list-items/by-price-list/{priceListId}")
    public ResponseEntity<List<PriceListItem>> findByPriceList(
            @PathVariable Long priceListId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get PriceListItems by priceListId={} page={}", priceListId, pageable);

        Page<PriceListItem> page = priceListItemService.findByPriceList(priceListId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/price-list-items/{id}")
    public ResponseEntity<PriceListItemResponseVM> findOne(@PathVariable Long id) {
        LOG.debug("REST request to get PriceListItem id={}", id);

        return priceListItemService.findOne(id)
                .map(PriceListItemResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/price-list-items/{id}/toggle-active")
    public ResponseEntity<PriceListItemResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST request to toggle active PriceListItem id={}", id);

        return priceListItemService.toggleIsActive(id)
                .map(PriceListItemResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private void validateItemTargetByType(
            BillingItemTypes type,
            Long serviceId,
            Long brandMedicationId,
            Long diagnosticTestId,
            Long procedureId
    ) {
        if (type == null) {
            throw new BadRequestAlertException(
                    "itemTypeRequired",
                    "priceListItem",
                    "Item type is required"
            );
        }

        switch (type) {
            case SERVICE -> {
                if (serviceId == null || brandMedicationId != null || diagnosticTestId != null || procedureId != null) {
                    throw new BadRequestAlertException(
                            "invalidServiceTarget",
                            "priceListItem",
                            "SERVICE item must have serviceId only"
                    );
                }
            }
            case MEDICATION -> {
                if (brandMedicationId == null || serviceId != null || diagnosticTestId != null || procedureId != null) {
                    throw new BadRequestAlertException(
                            "invalidMedicationTarget",
                            "priceListItem",
                            "MEDICATION item must have brandMedicationId only"
                    );
                }
            }
            case LABORATORY, RADIOLOGY, PATHOLOGY -> {
                if (diagnosticTestId == null || serviceId != null || brandMedicationId != null || procedureId != null) {
                    throw new BadRequestAlertException(
                            "invalidDiagnosticTarget",
                            "priceListItem",
                            "LABORATORY/RADIOLOGY/PATHOLOGY item must have diagnosticTestId only"
                    );
                }
            }
            case PROCEDURE -> {
                if (procedureId == null || serviceId != null || brandMedicationId != null || diagnosticTestId != null) {
                    throw new BadRequestAlertException(
                            "invalidProcedureTarget",
                            "priceListItem",
                            "PROCEDURE item must have procedureId only"
                    );
                }
            }
            default -> throw new BadRequestAlertException(
                    "unsupportedItemType",
                    "priceListItem",
                    "Unsupported item type: " + type
            );
        }
    }
}