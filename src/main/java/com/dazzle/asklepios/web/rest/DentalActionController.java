package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DentalAction;
import com.dazzle.asklepios.domain.enumeration.DentalActionType;
import com.dazzle.asklepios.service.DentalActionService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.dentalaction.DentalActionCreateVM;
import com.dazzle.asklepios.web.rest.vm.dentalaction.DentalActionResponseVM;
import com.dazzle.asklepios.web.rest.vm.dentalaction.DentalActionUpdateVM;
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
 * REST controller for managing {@link com.dazzle.asklepios.domain.DentalAction}.
 *
 * <p>Provides CRUD endpoints for creating, reading, updating, and deleting dental actions.</p>
 */
@RestController
@RequestMapping("/api/setup/dental-actions")
public class DentalActionController {

    private static final Logger LOG = LoggerFactory.getLogger(DentalActionController.class);
    private final DentalActionService service;

    public DentalActionController(DentalActionService service) {
        this.service = service;
    }

    /**
     * {@code POST /api/setup/dental-actions} : Create a new DentalAction.
     *
     * <p>Validates and persists the entity, then returns the created record.</p>
     *
     * @param vm the request payload containing new dental action details.
     * @return {@link ResponseEntity} with status {@code 201 (Created)} and the created {@link DentalActionResponseVM}.
     */
    @PostMapping
    public ResponseEntity<DentalActionResponseVM> create(@Valid @RequestBody DentalActionCreateVM vm) {
        LOG.debug("REST request to create DentalAction : {}", vm);
        DentalAction entity = vm.toEntity();
        DentalAction saved = service.create(entity);

        return ResponseEntity
                .created(URI.create("/api/setup/dental-actions/" + saved.getId()))
                .body(DentalActionResponseVM.fromEntity(saved));
    }

    /**
     * {@code PUT /api/setup/dental-actions/{id}} : Update an existing DentalAction.
     *
     * <p>Returns {@code 404 (Not Found)} if the entity does not exist.</p>
     *
     * @param id the ID of the entity to update.
     * @param vm the updated payload.
     * @return {@link ResponseEntity} with status {@code 200 (OK)} and the updated record,
     *         or {@code 404 (Not Found)} if no entity with the given ID exists.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DentalActionResponseVM> update(
            @PathVariable Long id,
            @Valid @RequestBody DentalActionUpdateVM vm
    ) {
        LOG.debug("REST request to update DentalAction id={} with payload={}", id, vm);
        Optional<DentalAction> updated = service.update(id, vm.toEntity());

        return updated
                .map(DentalActionResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code GET /api/setup/dental-actions} : Get all DentalActions (paginated).
     *
     * @param pageable pagination information.
     * @return paginated list of {@link DentalActionResponseVM} with headers for pagination.
     */
    @GetMapping
    public ResponseEntity<List<DentalActionResponseVM>> findAll(@ParameterObject Pageable pageable) {
        LOG.debug("REST request to get all DentalActions page={}", pageable);
        Page<DentalAction> page = service.findAll(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent().stream()
                        .map(DentalActionResponseVM::fromEntity)
                        .toList(),
                headers,
                HttpStatus.OK
        );
    }

    /**
     * {@code GET /api/setup/dental-actions/{id}} : Get a DentalAction by ID.
     *
     * @param id the ID of the requested dental action.
     * @return {@link ResponseEntity} with the entity data, or {@code 404 (Not Found)} if absent.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DentalActionResponseVM> findOne(@PathVariable Long id) {
        LOG.debug("REST request to get DentalAction id={}", id);
        return service.findOne(id)
                .map(DentalActionResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code DELETE /api/setup/dental-actions/{id}} : Delete a DentalAction by ID.
     *
     * @param id the ID of the entity to delete.
     * @return {@link ResponseEntity} with status {@code 204 (No Content)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST request to delete DentalAction id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code GET /api/setup/dental-actions/by-type/{type}} :
     * Get paginated DentalActions by type.
     *
     * @param type the type of the dental action (Enum name).
     * @param pageable pagination parameters.
     * @return paginated list of {@link DentalActionResponseVM}.
     */
    @GetMapping("/by-type/{type}")
    public ResponseEntity<List<DentalActionResponseVM>> findByType(
            @PathVariable DentalActionType type,
            @ParameterObject Pageable pageable) {

        LOG.debug("REST request to get DentalActions by type={} page={}", type, pageable);
        Page<DentalAction> page = service.findByType(type, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent().stream()
                        .map(DentalActionResponseVM::fromEntity)
                        .toList(),
                headers,
                HttpStatus.OK
        );
    }

    /**
     * {@code GET /api/setup/dental-actions/by-description/{description}} :
     * Get paginated DentalActions containing a keyword in description.
     *
     * @param description text to search for (case-insensitive).
     * @param pageable pagination parameters.
     * @return paginated list of {@link DentalActionResponseVM}.
     */
    @GetMapping("/by-description/{description}")
    public ResponseEntity<List<DentalActionResponseVM>> findByDescription(
            @PathVariable String description,
            @ParameterObject Pageable pageable) {

        LOG.debug("REST request to get DentalActions by description='{}' page={}", description, pageable);
        Page<DentalAction> page = service.findByDescription(description, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent().stream()
                        .map(DentalActionResponseVM::fromEntity)
                        .toList(),
                headers,
                HttpStatus.OK
        );
    }

}
