package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import com.dazzle.asklepios.domain.enumeration.Screen;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.service.FacilityService;
import com.dazzle.asklepios.web.rest.vm.FacilityCreateVM;
import com.dazzle.asklepios.web.rest.vm.FacilityResponseVM;
import com.dazzle.asklepios.web.rest.vm.FacilityUpdateVM;
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
@RequestMapping("/api/setup/facility")
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
    public ResponseEntity<FacilityResponseVM> createFacility(@Valid @RequestBody FacilityCreateVM facilityVM) {
        LOG.debug("REST request to save Facility : {}", facilityVM);

        if (facilityVM.name() != null && facilityRepository.existsByNameIgnoreCase(facilityVM.name())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

         FacilityResponseVM created = facilityService.create(facilityVM);

         return ResponseEntity
                .created(URI.create("/api/facility/" + created.id()))
                .body(created);
    }



    @PutMapping("/{id}")
    public ResponseEntity<FacilityResponseVM> updateFacility(
            @PathVariable Long id,
            @Valid @RequestBody FacilityUpdateVM facilityVM) {

        LOG.debug("REST request to update Facility : {}, {}", id, facilityVM);

        return facilityService.update(id, facilityVM)
                .map(FacilityResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    /**
     * {@code GET /api/facility} : Get all facilities (cached).
     * <p>
     * Returns the full list of facilities without pagination.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of all facilities.
     */
    @GetMapping
    public ResponseEntity<List<FacilityResponseVM>> getAllFacilities() {
        LOG.debug("REST request to get all Facilities (cached)");
        List<FacilityResponseVM> facilities = facilityService.findAll();
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
    public ResponseEntity<FacilityResponseVM> getFacility(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Facility : {}", id);
        return facilityService.findOne(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
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
        boolean deleted = facilityService.delete(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/facility-types")
    public ResponseEntity<List<FacilityType>> getFacilityType() {
        List<FacilityType> facilityType = Arrays.asList(FacilityType.values());
        return ResponseEntity.ok(facilityType); // status 200 + body
    }

    @GetMapping("/currencies")
    public ResponseEntity<List<Currency>> getCurrency() {
        List<Currency> currencies = Arrays.asList(Currency.values());
        return ResponseEntity.ok(currencies); // status 200 + body
    }

    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }
}
