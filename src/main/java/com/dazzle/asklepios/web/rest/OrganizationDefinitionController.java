package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.OrganizationDefinition;
import com.dazzle.asklepios.security.AuthoritiesConstants;
import com.dazzle.asklepios.service.OrganizationDefinitionService;
import com.dazzle.asklepios.web.rest.vm.organizationDefinition.OrganizationDefinitionCreateVM;
import com.dazzle.asklepios.web.rest.vm.organizationDefinition.OrganizationDefinitionResponseVM;
import com.dazzle.asklepios.web.rest.vm.organizationDefinition.OrganizationDefinitionUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class OrganizationDefinitionController {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationDefinitionController.class);

    private final OrganizationDefinitionService organizationDefinitionService;

    public OrganizationDefinitionController(OrganizationDefinitionService organizationDefinitionService) {
        this.organizationDefinitionService = organizationDefinitionService;
    }

    /**
     * {@code POST /organization-definition} : Create OrganizationDefinition (single instance only).
     */
    @PostMapping("/organization-definition")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<OrganizationDefinitionResponseVM> createOrganizationDefinition(
            @Valid @RequestBody OrganizationDefinitionCreateVM vm
    ) {
        LOG.debug("REST create OrganizationDefinition payload={}", vm);

        if (organizationDefinitionService.exists()) {
            return ResponseEntity.status(409).build();
        }

        OrganizationDefinition created = organizationDefinitionService.create(vm);

        return ResponseEntity
                .created(URI.create("/api/setup/organization-definition/" + created.getId()))
                .body(OrganizationDefinitionResponseVM.ofEntity(created));
    }

    /**
     * {@code PUT /organization-definition/{id}} : Update an existing OrganizationDefinition.
     */
    @PutMapping("/organization-definition/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<OrganizationDefinitionResponseVM> updateOrganizationDefinition(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationDefinitionUpdateVM vm
    ) {
        LOG.debug("REST update OrganizationDefinition id={} payload={}", id, vm);

        return organizationDefinitionService.update(id, vm)
                .map(updated -> ResponseEntity.ok(OrganizationDefinitionResponseVM.ofEntity(updated)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code GET /organization-definition} : Get all OrganizationDefinitions (no pagination).
     */
    @GetMapping("/organization-definition")
    public ResponseEntity<List<OrganizationDefinitionResponseVM>> getAllOrganizationDefinitions() {
        LOG.debug("REST get all OrganizationDefinitions");

        List<OrganizationDefinitionResponseVM> result = organizationDefinitionService.findAll()
                .stream()
                .map(OrganizationDefinitionResponseVM::ofEntity)
                .toList();

        return ResponseEntity.ok(result);
    }

    /**
     * {@code GET /organization-definition/{id}} : Get a single OrganizationDefinition by id.
     */
    @GetMapping("/organization-definition/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<OrganizationDefinitionResponseVM> getOrganizationDefinition(@PathVariable Long id) {
        LOG.debug("REST get OrganizationDefinition id={}", id);

        return organizationDefinitionService.findOne(id)
                .map(OrganizationDefinitionResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}