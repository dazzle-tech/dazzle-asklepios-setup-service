package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.CatalogDiagnosticTest;
import com.dazzle.asklepios.service.CatalogService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.catalog.CatalogAddTestsVM;
import com.dazzle.asklepios.web.rest.vm.catalog.CatalogTestVM;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/**
 * Manages the catalog_diagnostic_test link table.
 */
@RestController
@RequestMapping("/api/setup")
public class CatalogDiagnosticTestController {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogDiagnosticTestController.class);

    private final CatalogService catalogService;

    public CatalogDiagnosticTestController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /** LIST all links (pageable) — optional, useful for admin views */
    @GetMapping("/catalog-diagnostic-test")
    public ResponseEntity<List<CatalogDiagnosticTest>> listAll(@ParameterObject Pageable pageable) {
        Page<CatalogDiagnosticTest> page = catalogService.listTests(null, pageable); // null -> service can handle as findAll
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


    /** BULK ADD tests to a catalog */
    @PostMapping("/catalog/{catalogId:\\d+}/tests")
    public ResponseEntity<Void> addTests(@PathVariable Long catalogId,
                                         @Valid @RequestBody CatalogAddTestsVM vm) {
        LOG.debug("REST add tests to Catalog id={} tests={}", catalogId, vm.getTestIds());
        catalogService.addTests(catalogId, vm);
        return ResponseEntity.noContent().build();
    }


    /** DELETE by composite keys (catalog + test) */
    @DeleteMapping("/catalog/{catalogId:\\d+}/tests/{testId:\\d+}")
    public ResponseEntity<Void> removeTest(@PathVariable Long catalogId, @PathVariable Long testId) {
        catalogService.removeTest(catalogId, testId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/catalog/{catalogId}/tests")
    public ResponseEntity<CatalogTestVM> getTests(
            @PathVariable Long catalogId,
            Pageable pageable) {
        return ResponseEntity.ok(catalogService.getTestsForCatalog(catalogId, pageable));
    }

}
