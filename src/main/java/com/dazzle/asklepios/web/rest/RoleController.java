package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Role;
import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.repository.RoleRepository;
import com.dazzle.asklepios.service.RolePermissionService;
import com.dazzle.asklepios.service.RoleService;
import com.dazzle.asklepios.service.dto.RoleScreenRequest;
import com.dazzle.asklepios.web.rest.vm.RoleCreateVM;
import com.dazzle.asklepios.web.rest.vm.RoleResponseVM;
import com.dazzle.asklepios.web.rest.vm.RoleUpdateVM;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/setup/role")
public class RoleController {

    private static final Logger LOG = LoggerFactory.getLogger(RoleController.class);

    private final RolePermissionService rolePermissionService;
    private final RoleService roleService;
    private final RoleRepository roleRepository;

    public RoleController(RolePermissionService rolePermissionService, RoleService roleService, RoleRepository roleRepository) {
        this.rolePermissionService = rolePermissionService;
        this.roleService = roleService;
        this.roleRepository = roleRepository;
    }

    @PostMapping
    public ResponseEntity<RoleResponseVM> createRole(@Valid @RequestBody RoleCreateVM roleVM) {
        LOG.debug("REST request to save Role : {}", roleVM);

        if (roleRepository.existsByNameIgnoreCase(roleVM.name())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Role result = roleService.create(roleVM);

        return ResponseEntity
                .created(URI.create("/setup/api/role/" + result.getId()))
                .body(RoleResponseVM.ofEntity(result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleResponseVM> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateVM roleUpdateVM) {

        LOG.debug("REST request to update Role : {}, {}", id, roleUpdateVM);

        return roleService.update(id, roleUpdateVM)
                .map(RoleResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<RoleResponseVM>> getAllRoles() {
        LOG.debug("REST request to get all Roles");
        List<RoleResponseVM> roles = roleService.findAll().stream()
                .map(RoleResponseVM::ofEntity)
                .toList();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseVM> getRole(@PathVariable Long id) {
        return roleService.findOne(id)
                .map(RoleResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Role : {}", id);
        boolean deleted = roleService.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<RoleResponseVM>> getRolesByFacility(@PathVariable("facilityId") Long facilityId) {
        LOG.debug("REST request to get Roles by Facility id={}", facilityId);
        List<RoleResponseVM> roles = roleService.findByFacilityId(facilityId).stream()
                .map(RoleResponseVM::ofEntity)
                .toList();
        return ResponseEntity.ok(roles);
    }

    @PutMapping("/{roleId}/screens")
    public ResponseEntity<Void> updateRoleScreens(
            @PathVariable Long roleId,
            @RequestBody List<RoleScreenRequest> requests
    ) {
        rolePermissionService.updateRolePermissions(roleId, requests);
        return ResponseEntity.ok().build();
    }

    /**
     * Get all screens + operations for a role
     */
    @GetMapping("/{roleId}/screens")
    public ResponseEntity<List<RoleScreenRequest>> getRoleScreens(
            @PathVariable Long roleId
    ) {
        List<RoleScreenRequest> screens = rolePermissionService.getRoleScreens(roleId);
        return ResponseEntity.ok(screens);
    }
}
