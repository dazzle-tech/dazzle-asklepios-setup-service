package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.PriceList;
import com.dazzle.asklepios.domain.enumeration.biling.PriceListTypes;
import com.dazzle.asklepios.service.PriceListService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListResponseVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListSaveVM;
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
public class PriceListController {

    private static final Logger LOG = LoggerFactory.getLogger(PriceListController.class);

    private final PriceListService service;

    public PriceListController(PriceListService service) {
        this.service = service;
    }

    /**
     * Create or update price lists.
     * - vm.id != null => update single record
     * - vm.id == null => bulk create (one per facilityId; requires at least one facility)
     *
     * Returns LIST because bulk create may generate multiple records.
     */
    @PostMapping("/price-list")
    public ResponseEntity<List<PriceListResponseVM>> save(@RequestBody PriceListSaveVM vm) {
        LOG.debug("REST save PriceList payload={}", vm);

        boolean isUpdate = vm.id() != null;

        List<PriceList> saved = service.save(vm);

        List<PriceListResponseVM> body =
                saved.stream().map(PriceListResponseVM::ofEntity).toList();

        if (saved.isEmpty()) {
            return isUpdate
                    ? ResponseEntity.ok(body)
                    : ResponseEntity.status(HttpStatus.CREATED).body(body);
        }

        URI location = URI.create("/api/setup/price-list/" + saved.get(0).getId());

        if (isUpdate) {
            return ResponseEntity
                    .ok()
                    .location(location)
                    .body(body);
        }

        return ResponseEntity
                .created(location)
                .body(body);
    }

    @GetMapping("/price-list")
    public ResponseEntity<List<PriceListResponseVM>> list(@ParameterObject Pageable pageable) {
        LOG.debug("REST request to list PriceLists page={}", pageable);

        Page<PriceList> page = service.findAll(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent().stream().map(PriceListResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/price-list/active")
    public ResponseEntity<List<PriceListResponseVM>> listActive(@ParameterObject Pageable pageable) {
        LOG.debug("REST request to list active PriceLists page={}", pageable);

        Page<PriceList> page = service.findAllActive(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent().stream().map(PriceListResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/price-list/{id}")
    public ResponseEntity<PriceListResponseVM> get(@PathVariable Long id) {
        LOG.debug("REST request to get PriceList id={}", id);

        return service.findOne(id)
                .map(PriceListResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/price-list/by-type/{type}")
    public ResponseEntity<List<PriceListResponseVM>> findByType(
            @PathVariable PriceListTypes type,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to find PriceLists by type={} page={}", type, pageable);

        Page<PriceList> page = service.findByType(type, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent().stream().map(PriceListResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/price-list/by-name/{name}")
    public ResponseEntity<List<PriceListResponseVM>> findByName(
            @PathVariable String name,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to find PriceLists by name='{}' page={}", name, pageable);

        Page<PriceList> page = service.findByName(name, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent().stream().map(PriceListResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/price-list/by-type-and-name/{type}/{name}")
    public ResponseEntity<List<PriceListResponseVM>> findByTypeAndName(
            @PathVariable PriceListTypes type,
            @PathVariable String name,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to find PriceLists by type={} and name='{}' page={}", type, name, pageable);

        Page<PriceList> page = service.findByTypeAndName(type, name, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent().stream().map(PriceListResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @PatchMapping("/price-list/{id}/toggle-active")
    public ResponseEntity<PriceListResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle PriceList isActive id={}", id);

        return service.toggleIsActive(id)
                .map(PriceListResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
