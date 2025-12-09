package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.PriceListAttribute;
import com.dazzle.asklepios.service.PriceListAttributeService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.pricelist.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class PriceListAttributeController {

    private static final Logger LOG = LoggerFactory.getLogger(PriceListAttributeController.class);

    private final PriceListAttributeService service;

    public PriceListAttributeController(PriceListAttributeService service) {
        this.service = service;
    }

    @PostMapping("/price-list-attributes")
    public ResponseEntity<PriceListAttributeResponseVM> create(@Valid @RequestBody PriceListAttributeSaveVM vm) {
        LOG.debug("REST request to create PriceListAttribute : {}", vm);
        PriceListAttribute saved = service.create(vm);
        URI location = URI.create("/api/setup/price-list-attributes/" + saved.getId());
        return ResponseEntity.created(location).body(PriceListAttributeResponseVM.ofEntity(saved));
    }

    @PutMapping("/price-list-attributes")
    public ResponseEntity<PriceListAttributeResponseVM> update(@Valid @RequestBody PriceListAttributeUpdateVM vm) {
        LOG.debug("REST request to update PriceListAttribute : {}", vm);
        PriceListAttribute saved = service.update(vm);
        URI location = URI.create("/api/setup/price-list-attributes/" + saved.getId());
        return ResponseEntity.ok().location(location).body(PriceListAttributeResponseVM.ofEntity(saved));
    }

    @GetMapping("/price-list-attributes/{id}")
    public ResponseEntity<PriceListAttributeResponseVM> getOne(@PathVariable Long id) {
        LOG.debug("REST request to get PriceListAttribute : {}", id);
        return service.findOne(id)
                .map(PriceListAttributeResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/price-list-attributes/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST request to delete PriceListAttribute : {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/price-list-attributes/{id}/toggle-active")
    public ResponseEntity<PriceListAttributeResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST request to toggle active PriceListAttribute : {}", id);
        return service.toggleActive(id)
                .map(PriceListAttributeResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/price-list-attributes")
    public ResponseEntity<List<PriceListAttributeResponseVM>> getAll(@ParameterObject Pageable pageable) {
        LOG.debug("REST request to get all PriceListAttributes");
        Page<PriceListAttribute> page = service.getAll(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(PriceListAttributeResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/price-list-attributes/active")
    public ResponseEntity<List<PriceListAttributeResponseVM>> getAllActive(@ParameterObject Pageable pageable) {
        LOG.debug("REST request to get all active PriceListAttributes");
        Page<PriceListAttribute> page = service.getAllActive(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(PriceListAttributeResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/price-list-attributes/by-price-list/{priceListId}")
    public ResponseEntity<List<PriceListAttributeResponseVM>> getByPriceList(
            @PathVariable Long priceListId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get PriceListAttributes by priceListId={}", priceListId);
        Page<PriceListAttribute> page = service.getByPriceList(priceListId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(PriceListAttributeResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/price-list-attributes/by-price-list/{priceListId}/active")
    public ResponseEntity<List<PriceListAttributeResponseVM>> getActiveByPriceList(
            @PathVariable Long priceListId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get active PriceListAttributes by priceListId={}", priceListId);
        Page<PriceListAttribute> page = service.getActiveByPriceList(priceListId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(PriceListAttributeResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }
}
