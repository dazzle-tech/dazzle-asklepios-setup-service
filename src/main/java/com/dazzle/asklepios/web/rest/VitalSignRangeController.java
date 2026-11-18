package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.enumeration.VitalSignType;
import com.dazzle.asklepios.service.VitalSignRangeService;
import com.dazzle.asklepios.web.rest.vm.vitalsignrange.VitalSignRangeCreateVM;
import com.dazzle.asklepios.web.rest.vm.vitalsignrange.VitalSignRangeResponseVM;
import com.dazzle.asklepios.web.rest.vm.vitalsignrange.VitalSignRangeUpdateVM;
import com.dazzle.asklepios.web.rest.vm.vitalsignrange.VitalSignTypeVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import com.dazzle.asklepios.security.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * Normal ranges for Vital Signs. Reading is open to any authenticated user (staff entering vital signs);
 * editing requires the Vital Sign Setup EDIT permission.
 */
@RestController
@RequestMapping("/api/setup/vital-sign-ranges")
public class VitalSignRangeController {

    private static final String EDIT_AUTHORITY = "VITAL_SIGN_SETUP_EDIT";
    private static final Logger LOG = LoggerFactory.getLogger(VitalSignRangeController.class);

    private final VitalSignRangeService service;

    public VitalSignRangeController(VitalSignRangeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<VitalSignRangeResponseVM>> findAll() {
        LOG.debug("REST list VitalSignRange");
        return ResponseEntity.ok(
                service.findAll().stream().map(VitalSignRangeResponseVM::fromEntity).toList());
    }

    /** The six measurement keys and units, so vital signs without any rule yet stay selectable. */
    @GetMapping("/vital-signs")
    public ResponseEntity<List<VitalSignTypeVM>> findVitalSigns() {
        return ResponseEntity.ok(
                Arrays.stream(VitalSignType.values()).map(VitalSignTypeVM::fromType).toList());
    }

    @PostMapping
    public ResponseEntity<VitalSignRangeResponseVM> create(@Valid @RequestBody VitalSignRangeCreateVM vm) {
        requireEditPermission();
        LOG.debug("REST create VitalSignRange payload={}", vm);
        VitalSignRangeResponseVM saved = VitalSignRangeResponseVM.fromEntity(service.create(
                vm.vitalSignKey(), vm.ageFromMonths(), vm.ageToMonths(), vm.normalMin(), vm.normalMax()));
        return ResponseEntity.created(URI.create("/api/setup/vital-sign-ranges/" + saved.id())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VitalSignRangeResponseVM> update(
            @PathVariable Long id,
            @Valid @RequestBody VitalSignRangeUpdateVM vm
    ) {
        requireEditPermission();
        LOG.debug("REST update VitalSignRange id={} payload={}", id, vm);
        return ResponseEntity.ok(VitalSignRangeResponseVM.fromEntity(
                service.update(id, vm.ageFromMonths(), vm.ageToMonths(), vm.normalMin(), vm.normalMax())));
    }

    /** Writes need the Vital Sign Setup EDIT authority (granted through Access Roles). */
    private static void requireEditPermission() {
        if (!SecurityUtils.hasJwtAuthority(EDIT_AUTHORITY)) {
            throw new AccessDeniedException("Vital Sign Setup EDIT permission is required");
        }
    }
}
