package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.PolicyDefinition;
import com.dazzle.asklepios.security.AuthoritiesConstants;
import com.dazzle.asklepios.service.PolicyDefinitionService;
import com.dazzle.asklepios.service.dto.PolicyDefinition.PolicyDefinitionCreateDTO;
import com.dazzle.asklepios.service.dto.PolicyDefinition.PolicyDefinitionUpdateDTO;
import com.dazzle.asklepios.web.rest.vm.policyDefinition.PolicyDefinitionResponseVM;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequiredArgsConstructor

public class PolicyDefinitionController {

    private static final Logger LOG = LoggerFactory.getLogger(PolicyDefinitionController.class);

    private final PolicyDefinitionService policyDefinitionService;

    @PostMapping("/policy-definition")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<PolicyDefinitionResponseVM> createPolicyDefinition(
            @Valid @RequestBody PolicyDefinitionCreateDTO policyDefinitionCreateDTO
    ) {
        LOG.debug("REST request to create PolicyDefinition : {}", policyDefinitionCreateDTO);

        PolicyDefinition created = policyDefinitionService.create(policyDefinitionCreateDTO);

        return ResponseEntity
                .created(URI.create("/api/setup/policy-definition/" + created.getId()))
                .body(PolicyDefinitionResponseVM.ofEntity(created));
    }

    @PutMapping("/policy-definition")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<PolicyDefinitionResponseVM> updatePolicyDefinition(
            @Valid @RequestBody PolicyDefinitionUpdateDTO policyDefinitionUpdateDTO
    ) {
        LOG.debug("REST request to update PolicyDefinition : {}", policyDefinitionUpdateDTO);

        PolicyDefinition updated = policyDefinitionService.update(policyDefinitionUpdateDTO);
        return ResponseEntity.ok(PolicyDefinitionResponseVM.ofEntity(updated));
    }

    @GetMapping("/policy-definition")
    public ResponseEntity<List<PolicyDefinitionResponseVM>> getAllPolicyDefinition(
            @ParameterObject Pageable pageable) {
        LOG.debug("REST get all policy definition");
        Page<PolicyDefinition> page = policyDefinitionService.findAll(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<PolicyDefinitionResponseVM> body = page.getContent().stream()
                .map(PolicyDefinitionResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/policy-definition/by-facility")
    public ResponseEntity<List<PolicyDefinitionResponseVM>> getAllPolicyDefinitionByFacility(
            @RequestParam Long facilityId,
            @ParameterObject Pageable pageable) {
        LOG.debug("REST get policy definition by facilityId={}", facilityId);
        Page<PolicyDefinition> page = policyDefinitionService.findByFacilityId(facilityId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<PolicyDefinitionResponseVM> body = page.getContent().stream()
                .map(PolicyDefinitionResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/policy-definition/by-code")
    public ResponseEntity<List<PolicyDefinitionResponseVM>> getAllPolicyDefinitionByCode(
            @RequestParam String code,
            @ParameterObject Pageable pageable) {
        LOG.debug("REST get policy definition by code='{}'", code);
        Page<PolicyDefinition> page = policyDefinitionService.findByCode(code, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<PolicyDefinitionResponseVM> body = page.getContent().stream()
                .map(PolicyDefinitionResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/policy-definition/by-name")
    public ResponseEntity<List<PolicyDefinitionResponseVM>> getAllPolicyDefinitionByName(
            @RequestParam String name,
            @ParameterObject Pageable pageable) {
        LOG.debug("REST get policy definition by name='{}'", name);
        Page<PolicyDefinition> page = policyDefinitionService.findByName(name, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<PolicyDefinitionResponseVM> body = page.getContent().stream()
                .map(PolicyDefinitionResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/policy-definition/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<PolicyDefinitionResponseVM> getOnePolicyDefinitionById(@PathVariable Long id) {
        LOG.debug("REST request to get PolicyDefinition : {}", id);

        return policyDefinitionService.findOne(id)
                .map(PolicyDefinitionResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/policy-definition/{id}/toggle-active")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<PolicyDefinitionResponseVM> togglePolicyDefinitionActive(@PathVariable Long id) {
        LOG.debug("REST request to toggle active PolicyDefinition : {}", id);

        return policyDefinitionService.toggleActive(id)
                .map(PolicyDefinitionResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
