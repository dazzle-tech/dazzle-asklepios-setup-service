package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Catalog;
import com.dazzle.asklepios.domain.enumeration.TestType;
import com.dazzle.asklepios.service.CatalogService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.catalog.CatalogCreateVM;
import com.dazzle.asklepios.web.rest.vm.catalog.CatalogResponseVM;
import com.dazzle.asklepios.web.rest.vm.catalog.CatalogUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
public class CatalogController {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogController.class);

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /** CREATE */
    @PostMapping("/catalog")
    public ResponseEntity<CatalogResponseVM> create(@Valid @RequestBody CatalogCreateVM vm) {
        LOG.debug("REST create Catalog payload={}", vm);
        Catalog created = catalogService.create(vm);
        return ResponseEntity
                .created(URI.create("/api/setup/catalog/" + created.getId()))
                .body(CatalogResponseVM.ofEntity(created));
    }

    /** UPDATE */
    @PutMapping("/catalog/{id}")
    public ResponseEntity<CatalogResponseVM> update(@PathVariable Long id,
                                                    @Valid @RequestBody CatalogUpdateVM vm) {
        LOG.debug("REST update Catalog id={} payload={}", id, vm);
        return catalogService.update(id, vm)
                .map(c -> ResponseEntity.ok(CatalogResponseVM.ofEntity(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** LIST (pageable) */
    @GetMapping("/catalog")
    public ResponseEntity<List<CatalogResponseVM>> list(@ParameterObject Pageable pageable) {
        Page<Catalog> page = catalogService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent().stream().map(CatalogResponseVM::ofEntity).toList(), headers, HttpStatus.OK);
    }

    /** GET ONE */
    @GetMapping("/catalog/{id}")
    public ResponseEntity<CatalogResponseVM> get(@PathVariable Long id) {
        return catalogService.findOne(id)
                .map(CatalogResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    /** FILTERS / SEARCH (pageable) */
    @GetMapping("/catalog/by-department/{departmentId:\\d+}")
    public ResponseEntity<List<CatalogResponseVM>> byDepartment(@PathVariable Long departmentId,
                                                                @ParameterObject Pageable pageable) {
        Page<Catalog> page = catalogService.findByDepartment(departmentId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent().stream().map(CatalogResponseVM::ofEntity).toList(), headers, HttpStatus.OK);
    }

    @GetMapping("/catalog/by-type/{type}")
    public ResponseEntity<List<CatalogResponseVM>> byType(@PathVariable TestType type,
                                                          @ParameterObject Pageable pageable) {
        Page<Catalog> page = catalogService.findByType(type, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent().stream().map(CatalogResponseVM::ofEntity).toList(), headers, HttpStatus.OK);
    }

    @GetMapping("/catalog/by-name/{name}")
    public ResponseEntity<List<CatalogResponseVM>> byName(@PathVariable String name,
                                                          @ParameterObject Pageable pageable) {
        Page<Catalog> page = catalogService.searchByName(name, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent().stream().map(CatalogResponseVM::ofEntity).toList(), headers, HttpStatus.OK);
    }

    /** DELETE by composite key (catalog) */
    @DeleteMapping("/catalog/{catalogId}")
    public ResponseEntity<Void> removeCatalog(@PathVariable Long catalogId) {
        catalogService.removeCatalog(catalogId);
        return ResponseEntity.noContent().build();
    }
}
