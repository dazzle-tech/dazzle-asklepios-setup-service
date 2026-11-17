// src/main/java/com/dazzle/asklepios/web/rest/ConfigurationController.java
package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Configuration;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.enumeration.ConfigurationKeys;
import com.dazzle.asklepios.domain.enumeration.ConfigurationReferenceType;
import com.dazzle.asklepios.domain.enumeration.ConfigurationValueType;
import com.dazzle.asklepios.security.AuthoritiesConstants;
import com.dazzle.asklepios.service.ConfigurationService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.configuration.ConfigurationCreateVM;
import com.dazzle.asklepios.web.rest.vm.configuration.ConfigurationUpdateVM;
import com.dazzle.asklepios.web.rest.vm.configuration.ConfigurationUserOptionVM;
import com.dazzle.asklepios.web.rest.vm.configuration.ResolvedConfigurationVM;
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
import java.util.Set;

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
     * - Organization: one row per key, facility empty and users empty.
     * - Facility: one row per (facility, key), users empty.
     * - User: one row per (facility, key, user). The same key may still exist at facility and organization level.
     * - A user selection requires a facility.
     * - Override rule:
     * - A facility row overrides the organization row for that facility only.
     * - A user row overrides the facility row for that user at that facility only.
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
            throw duplicateKeyException(vm.key(), vm.facilityId(), vm.userIds());
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
            throw duplicateKeyException(vm.key(), vm.facilityId(), vm.userIds());
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
     * Stored row that wins for this user and facility.
     * Order: user → facility → organization. 404 when none of those rows exist.
     */
    @GetMapping("/configuration/effective/{key}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Configuration> getEffective(
            @PathVariable ConfigurationKeys key,
            @RequestParam(value = "facilityId", required = false) Long facilityId,
            @RequestParam(value = "userId", required = false) Long userId
    ) {
        return configurationService.findEffective(key, userId, facilityId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Feature check. Most specific active row wins:
     * user → facility → organization. 404 when none of those rows exist.
     */
    @GetMapping("/configuration/resolve/{key}")
    public ResponseEntity<ResolvedConfigurationVM> resolve(
            @PathVariable ConfigurationKeys key,
            @RequestParam(value = "facilityId", required = false) Long facilityId,
            @RequestParam(value = "userId", required = false) Long userId
    ) {
        return configurationService.getConfig(key, userId, facilityId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Same resolution as {@link #resolve} for service-to-service calls.
     */
    @GetMapping("/configuration/internalJob/{key}")
    public ResponseEntity<ResolvedConfigurationVM> resolveInternal(
            @PathVariable ConfigurationKeys key,
            @RequestParam(value = "facilityId", required = false) Long facilityId,
            @RequestParam(value = "userId", required = false) Long userId
    ) {
        return configurationService.getConfig(key, userId, facilityId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Searchable user field for the configuration form.
     * Pass facilityId after a facility is selected to limit the list to that clinic.
     */
    @GetMapping("/configuration/users/search")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<ConfigurationUserOptionVM>> searchUsers(
            @RequestParam(value = "searchText", required = false) String searchText,
            @RequestParam(value = "facilityId", required = false) Long facilityId,
            @ParameterObject Pageable pageable
    ) {
        Page<User> page = configurationService.searchUsers(searchText, facilityId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );
        List<ConfigurationUserOptionVM> body = page.getContent().stream()
                .map(ConfigurationUserOptionVM::ofEntity)
                .toList();
        return ResponseEntity.ok().headers(headers).body(body);
    }

    private BadRequestAlertException duplicateKeyException(ConfigurationKeys key, Long facilityId, Set<Long> userIds) {
        String scope;
        if (userIds != null && !userIds.isEmpty() && facilityId != null) {
            scope = "user at facility " + facilityId;
        } else if (facilityId != null) {
            scope = "facility " + facilityId;
        } else {
            scope = "organization";
        }

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

    @GetMapping("/configuration/search")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<Configuration>> quickSearch(
            @RequestParam("searchText") String searchText,
            @ParameterObject Pageable pageable
    ) {
        Page<Configuration> page = configurationService.quickSearch(searchText, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/configuration/filter/value-type")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<Configuration>> filterByValueType(
            @RequestParam("valueType") ConfigurationValueType valueType,
            @ParameterObject Pageable pageable
    ) {
        Page<Configuration> page = configurationService.filterByValueType(valueType, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/configuration/filter/reference-type")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<Configuration>> filterByReferenceType(
            @RequestParam("referenceType") ConfigurationReferenceType referenceType,
            @ParameterObject Pageable pageable
    ) {
        Page<Configuration> page = configurationService.filterByReferenceType(referenceType, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}

