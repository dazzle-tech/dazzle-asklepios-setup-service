package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.PolicyAssignment;
import com.dazzle.asklepios.domain.enumeration.PolicyResourceType;
import com.dazzle.asklepios.service.PolicyAssignmentService;
import com.dazzle.asklepios.service.dto.policyAssignment.PolicyAssignmentCreateDTO;
import com.dazzle.asklepios.service.dto.policyAssignment.PolicyAssignmentUpdateDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class PolicyAssignmentController {

    private static final Logger LOG = LoggerFactory.getLogger(PolicyAssignmentController.class);

    private final PolicyAssignmentService policyAssignmentService;

    public PolicyAssignmentController(PolicyAssignmentService policyAssignmentService) {
        this.policyAssignmentService = policyAssignmentService;
    }

    @PostMapping("/policy-assignments")
    public ResponseEntity<PolicyAssignment> create(@Valid @RequestBody PolicyAssignmentCreateDTO dto) {
        LOG.debug("REST request to create PolicyAssignment: {}", dto);

        PolicyAssignment result = policyAssignmentService.create(dto);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/policy-assignments")
    public ResponseEntity<PolicyAssignment> update(@Valid @RequestBody PolicyAssignmentUpdateDTO dto) {
        LOG.debug("REST request to update PolicyAssignment isRequired: {}", dto);

        PolicyAssignment result = policyAssignmentService.update(dto);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/policy-assignments/{id}")
    public ResponseEntity<PolicyAssignment> getOne(@PathVariable Long id) {
        LOG.debug("REST request to get PolicyAssignment by id: {}", id);

        PolicyAssignment result = policyAssignmentService.getOne(id);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/policy-assignments")
    public ResponseEntity<List<PolicyAssignment>> getAll() {
        LOG.debug("REST request to get all PolicyAssignments");

        List<PolicyAssignment> result = policyAssignmentService.getAll();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/policy-assignments/resource")
    public ResponseEntity<List<PolicyAssignment>> getAllByResource(@RequestParam PolicyResourceType resourceType, @RequestParam Long resourceId) {
        LOG.debug("REST request to get PolicyAssignments by resourceType={}, resourceId={}", resourceType, resourceId);

        List<PolicyAssignment> result = policyAssignmentService.getAllByResource(resourceType, resourceId);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/policy-assignments/active")
    public ResponseEntity<List<PolicyAssignment>> getActiveByFacilityAndResource(@RequestParam Long facilityId, @RequestParam PolicyResourceType resourceType, @RequestParam Long resourceId) {
        LOG.debug("REST request to get active PolicyAssignments by facilityId={}, resourceType={}, resourceId={}", facilityId, resourceType, resourceId);

        List<PolicyAssignment> result = policyAssignmentService.getActiveByFacilityAndResource(facilityId, resourceType, resourceId);

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/policy-assignments/{id}/toggle-active")
    public ResponseEntity<PolicyAssignment> toggleActive(@PathVariable Long id) {
        LOG.debug("REST request to toggle active PolicyAssignment id={}", id);

        PolicyAssignment result = policyAssignmentService.toggleActive(id);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/policy-assignments/active/effective")
    public ResponseEntity<List<PolicyAssignment>> getEffectiveActivePolicyAssignments(@RequestParam Long facilityId, @RequestParam Long departmentId, @RequestParam PolicyResourceType resourceType, @RequestParam Long resourceId) {
        LOG.debug("REST request to get effective active PolicyAssignments by facilityId={}, departmentId={}, resourceType={}, resourceId={}", facilityId, departmentId, resourceType, resourceId);

        List<PolicyAssignment> result = policyAssignmentService.getEffectiveActivePolicyAssignments(facilityId, departmentId, resourceType, resourceId);
        return ResponseEntity.ok(result);
    }
}