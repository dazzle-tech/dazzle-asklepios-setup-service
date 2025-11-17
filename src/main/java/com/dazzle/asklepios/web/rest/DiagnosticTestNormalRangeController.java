package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DiagnosticTestNormalRange;
import com.dazzle.asklepios.service.DiagnosticTestNormalRangeService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.normalrange.DiagnosticTestNormalRangeCreateVM;
import com.dazzle.asklepios.web.rest.vm.normalrange.DiagnosticTestNormalRangeResponseVM;
import com.dazzle.asklepios.web.rest.vm.normalrange.DiagnosticTestNormalRangeUpdateVM;
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
import java.util.Optional;

/**
 * REST controller for managing {@link com.dazzle.asklepios.domain.DiagnosticTestNormalRange}.
 */
@RestController
@RequestMapping("/api/setup/diagnostic-test-normal-ranges")
public class DiagnosticTestNormalRangeController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestNormalRangeController.class);
    private final DiagnosticTestNormalRangeService service;

    public DiagnosticTestNormalRangeController(DiagnosticTestNormalRangeService service) {
        this.service = service;
    }

    /**
     * {@code POST /diagnostic-test-normal-ranges} : Create a new DiagnosticTestNormalRange.
     *
     * <p>Validates the request body, persists the entity, and returns the created object.</p>
     *
     * @param vm the creation payload.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and body of the created range,
     *         or {@code 400 (Bad Request)} if the payload is invalid.
     */
    @PostMapping
    public ResponseEntity<DiagnosticTestNormalRangeResponseVM> create(
            @Valid @RequestBody DiagnosticTestNormalRangeCreateVM vm) {

        LOG.debug("REST request to create DiagnosticTestNormalRange payload={}", vm);
        DiagnosticTestNormalRange saved = service.create(vm.toEntity());

        LOG.info("Created DiagnosticTestNormalRange id={} for testId={}",
                saved.getId(), saved.getTest() != null ? saved.getTest().getId() : null);

        return ResponseEntity
                .created(URI.create("/api/setup/diagnostic-test-normal-ranges/" + saved.getId()))
                .body(DiagnosticTestNormalRangeResponseVM.fromEntity(saved));
    }

    /**
     * {@code PUT /diagnostic-test-normal-ranges/:id} : Update an existing DiagnosticTestNormalRange.
     *
     * <p>Updates the fields of the normal range identified by {@code id}.</p>
     *
     * @param id the identifier of the range to update.
     * @param vm the update payload.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the updated object,
     *         or {@code 404 (Not Found)} if the record does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DiagnosticTestNormalRangeResponseVM> update(
            @PathVariable Long id,
            @Valid @RequestBody DiagnosticTestNormalRangeUpdateVM vm) {

        LOG.debug("REST request to update DiagnosticTestNormalRange id={} payload={}", id, vm);
        DiagnosticTestNormalRange entity = vm.toEntity();

        Optional<DiagnosticTestNormalRange> updated = service.update(id, entity);

        return updated
                .map(DiagnosticTestNormalRangeResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code GET /diagnostic-test-normal-ranges} : Get a paginated list of all DiagnosticTestNormalRanges.
     *
     * <p>Supports standard pagination parameters: {@code page}, {@code size}, and {@code sort}.</p>
     *
     * @param pageable pagination parameters.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and a paginated list of ranges.
     */
    @GetMapping
    public ResponseEntity<List<DiagnosticTestNormalRangeResponseVM>> findAll(@ParameterObject Pageable pageable) {
        LOG.debug("REST request to list DiagnosticTestNormalRange page={}", pageable);

        Page<DiagnosticTestNormalRange> page = service.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream()
                        .map(DiagnosticTestNormalRangeResponseVM::fromEntity)
                        .toList(),
                headers,
                HttpStatus.OK
        );
    }

    /**
     * {@code GET /diagnostic-test-normal-ranges/:id} : Get a single DiagnosticTestNormalRange by id.
     *
     * @param id the identifier of the range to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the range details,
     *         or {@code 404 (Not Found)} if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiagnosticTestNormalRangeResponseVM> findOne(@PathVariable Long id) {
        LOG.debug("REST request to get DiagnosticTestNormalRange id={}", id);

        return service.findOne(id)
                .map(DiagnosticTestNormalRangeResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code GET /diagnostic-test-normal-ranges/by-test/:testId} :
     * Get a paginated list of DiagnosticTestNormalRanges by testId.
     *
     * @param testId the id of the related diagnostic test.
     * @param pageable pagination and sorting parameters.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of ranges.
     */
    @GetMapping("/by-test/{testId}")
    public ResponseEntity<List<DiagnosticTestNormalRangeResponseVM>> findAllByTestId(
            @PathVariable Long testId, @ParameterObject Pageable pageable) {

        LOG.debug("REST request to list DiagnosticTestNormalRange by testId={} page={}", testId, pageable);

        Page<DiagnosticTestNormalRange> page = service.findAllByTestId(testId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream()
                        .map(DiagnosticTestNormalRangeResponseVM::fromEntity)
                        .toList(),
                headers,
                HttpStatus.OK
        );
    }
    /**
     * {@code GET /diagnostic-test-normal-ranges/by-profile-test/:profileTestId} :
     * Get a paginated list of DiagnosticTestNormalRanges by testId.
     *
     * @param profileTestId the id of the related diagnostic test.
     * @param pageable pagination and sorting parameters.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of ranges.
     */
    @GetMapping("/by-profile-test/{profileTestId}")
    public ResponseEntity<List<DiagnosticTestNormalRangeResponseVM>> findAllByProfileTestId(
            @PathVariable Long profileTestId, @ParameterObject Pageable pageable) {

        LOG.debug("REST request to list DiagnosticTestNormalRange by profileTtestId={} page={}", profileTestId, pageable);

        Page<DiagnosticTestNormalRange> page = service.findAllByProfileTestId(profileTestId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream()
                        .map(DiagnosticTestNormalRangeResponseVM::fromEntity)
                        .toList(),
                headers,
                HttpStatus.OK
        );
    }
    /**
     * {@code DELETE /diagnostic-test-normal-ranges/:id} :
     * Delete a DiagnosticTestNormalRange by id.
     *
     * @param id the identifier of the range to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST request to delete DiagnosticTestNormalRange id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code GET /diagnostic-test-normal-ranges/:normalRangeId/lovs} :
     * Get all LOV keys associated with a specific normal range.
     *
     * @param normalRangeId the id of the diagnostic test normal range.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of LOV keys,
     *         or {@code 404 (Not Found)} if no LOVs exist for the given range.
     */
    @GetMapping("/{normalRangeId}/lovs")
    public ResponseEntity<List<String>> getLovsByNormalRangeId(@PathVariable Long normalRangeId) {
        LOG.debug("REST request to get LOVs for DiagnosticTestNormalRange id={}", normalRangeId);

        List<String> lovs = service.findLovsByNormalRangeId(normalRangeId);

        if (lovs.isEmpty()) {
            LOG.warn("No LOVs found for DiagnosticTestNormalRange id={}", normalRangeId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(lovs);
    }
}
