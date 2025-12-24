// src/main/java/com/dazzle/asklepios/web/rest/ConfigurationController.java
package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Configuration;
import com.dazzle.asklepios.domain.enumeration.ConfigurationKeys;
import com.dazzle.asklepios.security.AuthoritiesConstants;
import com.dazzle.asklepios.service.ConfigurationService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.configuration.ConfigurationCreateVM;
import com.dazzle.asklepios.web.rest.vm.configuration.ConfigurationUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class ConfigurationController {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationController.class);

    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * POST /configuration : Create configuration.
     * <p>
     * Validations:
     * - Prevent duplicates:
     * - Org level: only one row per key where facility is null.
     * - Facility level: only one row per (facility, key).
     * - Override rule:
     * - If key exists at org level and you create it for a facility, that is allowed.
     * It is an override for that facility only. Org-level row remains for other facilities.
     * <p>
     * Enforcement:
     * - Primary enforcement is DB unique indexes.
     * - API catches duplicate violations and returns a clean 400.
     */
    @PostMapping("/configuration")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Configuration> create(@Valid @RequestBody ConfigurationCreateVM vm) {
        LOG.debug("REST create Configuration payload={}", vm);
        try {
            Configuration created = configurationService.create(vm);
            return ResponseEntity
                    .created(URI.create("/api/setup/configuration/" + created.getId()))
                    .body(created);
        } catch (DataIntegrityViolationException ex) {
            throw duplicateKeyException(vm.key(), vm.facilityId());
        }
    }

    /**
     * PUT /configuration/{id} : Update configuration.
     * <p>
     * Same duplicate rules apply (DB enforced).
     */
    @PutMapping("/configuration/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Configuration> update(
            @PathVariable Long id,
            @Valid @RequestBody ConfigurationUpdateVM vm
    ) {
        LOG.debug("REST update Configuration id={} payload={}", id, vm);
        try {
            return configurationService.update(id, vm)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (DataIntegrityViolationException ex) {
            throw duplicateKeyException(vm.key(), vm.facilityId());
        }
    }


    /**
     * GET /configuration/{id} : Get one by id.
     */
    @GetMapping("/configuration/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Configuration> getOne(@PathVariable Long id) {
        LOG.debug("REST get Configuration id={}", id);
        return configurationService.findOne(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    /**
     * Optional helper endpoint: resolve effective configuration for a facility.
     * If facility override exists, return it. Else return org-level if present.
     */
    @GetMapping("/configuration/effective/{key}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Configuration> getEffective(
            @PathVariable ConfigurationKeys key,
            @RequestParam(value = "facilityId", required = false) Long facilityId
    ) {
        return configurationService.findByKeyAndFacility(key, facilityId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private BadRequestAlertException duplicateKeyException(ConfigurationKeys key, Long facilityId) {
        String scope = (facilityId == null)
                ? "organization"
                : "facility " + facilityId;

        return new BadRequestAlertException(
                "Duplicate configuration key '" + key + "' at " + scope + " level",
                "configuration",
                "duplicatekey"
        );
    }

    /**
     * GET /configuration : Paged list
     */
    @GetMapping("/configuration")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<Configuration>> getAll(@ParameterObject Pageable pageable) {
        LOG.debug("REST get all Configuration");

        Page<Configuration> page = configurationService.findAll(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
