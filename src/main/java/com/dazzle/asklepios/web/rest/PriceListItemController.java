package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.PriceListItem;
import com.dazzle.asklepios.service.PriceListItemService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemResponseVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemSaveVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class PriceListItemController {

    private static final Logger LOG = LoggerFactory.getLogger(PriceListItemController.class);

    private final PriceListItemService service;

    public PriceListItemController(PriceListItemService service) {
        this.service = service;
    }

    // -------- CREATE --------
    @PostMapping("/price-list-item")
    public ResponseEntity<PriceListItemResponseVM> create(@RequestBody PriceListItemSaveVM vm) {
        LOG.debug("REST create PriceListItem payload={}", vm);

        PriceListItem saved = service.create(vm);
        PriceListItemResponseVM body = PriceListItemResponseVM.ofEntity(saved);

        URI location = URI.create("/api/setup/price-list-item/" + saved.getId());

        return ResponseEntity.created(location).body(body);
    }

    // -------- UPDATE --------
    @PutMapping("/price-list-item")
    public ResponseEntity<PriceListItemResponseVM> update(@RequestBody PriceListItemUpdateVM vm) {
        LOG.debug("REST update PriceListItem payload={}", vm);

        PriceListItem saved = service.update(vm);
        PriceListItemResponseVM body = PriceListItemResponseVM.ofEntity(saved);

        URI location = URI.create("/api/setup/price-list-item/" + saved.getId());

        return ResponseEntity.ok().location(location).body(body);
    }

    // -------- READ --------
    @GetMapping("/price-list-item")
    public ResponseEntity<List<PriceListItemResponseVM>> list(@ParameterObject Pageable pageable) {
        LOG.debug("REST request to list PriceListItems page={}", pageable);

        Page<PriceListItem> page = service.findAll(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent().stream().map(PriceListItemResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/price-list-item/active")
    public ResponseEntity<List<PriceListItemResponseVM>> listActive(@ParameterObject Pageable pageable) {
        LOG.debug("REST request to list active PriceListItems page={}", pageable);

        Page<PriceListItem> page = service.findAllActive(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent().stream().map(PriceListItemResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/price-list-item/by-price-list/{priceListId}")
    public ResponseEntity<List<PriceListItemResponseVM>> listByPriceList(
            @PathVariable Long priceListId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to list by priceListId={} page={}", priceListId, pageable);

        Page<PriceListItem> page = service.findByPriceList(priceListId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent().stream().map(PriceListItemResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/price-list-item/{id}")
    public ResponseEntity<PriceListItemResponseVM> get(@PathVariable Long id) {
        LOG.debug("REST request to get PriceListItem id={}", id);

        return service.findOne(id)
                .map(PriceListItemResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/price-list-item/{id}/toggle-active")
    public ResponseEntity<PriceListItemResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle isActive id={}", id);

        return service.toggleIsActive(id)
                .map(PriceListItemResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
