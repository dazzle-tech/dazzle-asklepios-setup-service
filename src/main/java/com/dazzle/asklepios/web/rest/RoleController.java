package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Role;
import com.dazzle.asklepios.repository.RoleRepository;
import com.dazzle.asklepios.service.RoleService;
import com.dazzle.asklepios.web.rest.vm.RoleVM;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/setup/api/role")
public class RoleController {

    private static final Logger LOG = LoggerFactory.getLogger(RoleController.class);

    private final RoleService roleService;
    private final RoleRepository roleRepository;

    public RoleController(RoleService roleService, RoleRepository roleRepository) {
        this.roleService = roleService;
        this.roleRepository = roleRepository;
    }

    @PostMapping
    public ResponseEntity<RoleVM> createRole(@Valid @RequestBody RoleVM roleVM) {
        LOG.debug("REST request to save Role : {}", roleVM);

        if (roleRepository.existsByNameIgnoreCase(roleVM.name())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Role toCreate = Role.builder()
                .name(roleVM.name())
                .type(roleVM.type())
                .facilityId(roleVM.facilityId())
                .build();

        Role result = roleService.create(toCreate);

        return ResponseEntity
                .created(URI.create("/setup/api/role/" + result.getId()))
                .body(RoleVM.ofEntity(result));
    }

    /**
     * {@code PUT /api/role/{id}} : Update an existing Role.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoleVM> updateRole(@PathVariable("id") Long id, @Valid @RequestBody RoleVM roleVM) {
        LOG.debug("REST request to update Role : {}, {}", id, roleVM);
        Optional<Role> updated = roleService.update(id, roleVM);
        return updated
                .map(RoleVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code GET /api/role} : Get all Roles.
     */
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        LOG.debug("REST request to get all Roles");
        List<Role> roles = roleService.findAll();
        return ResponseEntity.ok(roles);
    }

    /**
     * {@code GET /api/role/{id}} : Get a Role by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleVM> getRole(@PathVariable Long id) {
        return roleService.findOne(id)
                .map(RoleVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code DELETE /api/role/{id}} : Delete a Role by id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Role : {}", id);
        boolean deleted = roleService.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code GET /api/role/facility/{facilityId}} : Get all Roles for a Facility.
     */
    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<Role>> getRolesByFacility(@PathVariable("facilityId") Long facilityId) {
        LOG.debug("REST request to get Roles by Facility id={}", facilityId);
        List<Role> roles = roleService.findByFacilityId(facilityId);
        return ResponseEntity.ok(roles);
    }
}
