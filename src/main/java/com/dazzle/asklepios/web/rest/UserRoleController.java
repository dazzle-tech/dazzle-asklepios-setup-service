package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.UserRoleService;
import com.dazzle.asklepios.web.rest.vm.UserRoleVM;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/setup/user-role")
@RequiredArgsConstructor
public class UserRoleController {

    private static final Logger LOG = LoggerFactory.getLogger(UserRoleController.class);

    private final UserRoleService userRoleService;

    @GetMapping
    public ResponseEntity<List<UserRoleVM>> getAll() {
        LOG.info("REST request to get all user roles");
        List<UserRoleVM> roles = userRoleService.findAll().stream().map(UserRoleVM::ofEntity).toList();
        LOG.debug("Found {} user roles", roles.size());
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<UserRoleVM>> getByUser(@PathVariable Long userId) {
        LOG.info("REST request to get user roles by userId={}", userId);
        List<UserRoleVM> roles = userRoleService.findByUserId(userId).stream().map(UserRoleVM::ofEntity).toList();
        LOG.debug("Found {} roles for userId={}", roles.size(), userId);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<List<UserRoleVM>> getByRole(@PathVariable Long roleId) {
        LOG.info("REST request to get users by roleId={}", roleId);
        List<UserRoleVM> roles = userRoleService.findByRoleId(roleId).stream().map(UserRoleVM::ofEntity).toList();
        LOG.debug("Found {} users for roleId={}", roles.size(), roleId);
        return ResponseEntity.ok(roles);
    }

    @PostMapping
    public ResponseEntity<UserRoleVM> add(@RequestBody UserRoleVM vm) {
        LOG.info("REST request to add UserRole: userId={}, roleId={}", vm.userId(), vm.roleId());
        UserRoleVM saved = UserRoleVM.ofEntity(userRoleService.save(vm.userId(), vm.roleId()));
        LOG.debug("UserRole added successfully: {}", saved);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody UserRoleVM vm) {
        LOG.info("REST request to delete UserRole: userId={}, roleId={}", vm.userId(), vm.roleId());
        userRoleService.delete(vm.userId(), vm.roleId());
        LOG.debug("UserRole deleted successfully: userId={}, roleId={}", vm.userId(), vm.roleId());
        return ResponseEntity.noContent().build();
    }
}
