package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.service.FacilityService;
import com.dazzle.asklepios.web.rest.vm.FacilityVM;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

@RestController
@RequestMapping("/setup/api/facility")
public class FacilityController {

    private static final Logger LOG = LoggerFactory.getLogger(FacilityController.class);

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
        Arrays.asList("id", "name", "type")
    );

    private final FacilityService facilityService;
    private final FacilityRepository facilityRepository;

    public FacilityController(FacilityService facilityService, FacilityRepository facilityRepository) {
        this.facilityService = facilityService;
        this.facilityRepository = facilityRepository;
    }

    /**
     * {@code POST /api/facility} : Create a new Facility.
     *
     * Creates a new facility entity from the provided view model.
     *
     * @param facilityVM the data used to create the facility.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the created facility,
     * or with status {@code 409 (Conflict)} if a facility with the same name already exists.
     */
    @PostMapping
    public ResponseEntity<Facility> createFacility(@Valid @RequestBody FacilityVM facilityVM) {
        LOG.debug("REST request to save Facility : {}", facilityVM);
        if (facilityVM.getName() != null && facilityRepository.existsByNameIgnoreCase(facilityVM.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Facility toCreate = Facility.builder()
                .name(facilityVM.getName())
                .type(facilityVM.getType())
                .emailAddress(facilityVM.getEmailAddress())
                .phone1(facilityVM.getPhone1())
                .phone2(facilityVM.getPhone2())
                .fax(facilityVM.getFax())
                .addressId(facilityVM.getAddressId())
                .defaultCurrencyLkey(facilityVM.getDefaultCurrencyLkey())
                .isValid(facilityVM.getIsValid() != null ? facilityVM.getIsValid() : true)
                .build();

        Facility result = facilityService.create(toCreate);
        return ResponseEntity
            .created(URI.create("/api/facility/" + result.getId()))
            .body(result);
    }

    /**
     * {@code PUT /api/facility/{id}} : Update an existing Facility.
     *
     * @param id the id of the facility to update.
     * @param facility the facility data to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated facility,
     * or with status {@code 404 (Not Found)} if the facility does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Facility> updateFacility(@PathVariable("id") Long id, @Valid @RequestBody Facility facility) {
        LOG.debug("REST request to update Facility : {}, {}", id, facility);
        Optional<Facility> updated = facilityService.update(id, facility);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code GET /api/facility} : Get all facilities (cached).
     *
     * Returns the full list of facilities without pagination.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of all facilities.
     */
    @GetMapping
    public ResponseEntity<List<Facility>> getAllFacilities() {
        LOG.debug("REST request to get all Facilities (cached)");
        List<Facility> facilities = facilityService.findAll();
        return ResponseEntity.ok(facilities);
    }

    /**
     * {@code GET /api/facility/{id}} : Get the facility by id.
     *
     * @param id the id of the facility to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the facility,
     * or with status {@code 404 (Not Found)} if it does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Facility> getFacility(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Facility : {}", id);
        return facilityService.findOne(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code DELETE /api/facility/{id}} : Delete the facility by id.
     *
     * @param id the id of the facility to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacility(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Facility : {}", id);
        facilityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }
}
