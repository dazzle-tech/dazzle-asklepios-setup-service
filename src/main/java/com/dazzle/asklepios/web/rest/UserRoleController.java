package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.UserRoleService;
import com.dazzle.asklepios.web.rest.vm.UserRoleVM;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setup/user-role")
@RequiredArgsConstructor
public class
UserRoleController {
    private static final Logger LOG = LoggerFactory.getLogger(UserRoleController.class);

    private final UserRoleService userRoleService;

    @GetMapping
    public ResponseEntity<List<UserRoleVM>> getAll() {
        return ResponseEntity.ok(
                userRoleService.findAll().stream().map(UserRoleVM::ofEntity).toList()
        );
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<UserRoleVM>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(
                userRoleService.findByUserId(userId).stream().map(UserRoleVM::ofEntity).toList()
        );
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<List<UserRoleVM>> getByRole(@PathVariable Long roleId) {
        return ResponseEntity.ok(
                userRoleService.findByRoleId(roleId).stream().map(UserRoleVM::ofEntity).toList()
        );
    }

    @PostMapping
    public ResponseEntity<UserRoleVM> add(@RequestBody UserRoleVM vm) {
        LOG.debug("REST request to add UserRole : {}", vm);
        return ResponseEntity.ok(
                UserRoleVM.ofEntity(userRoleService.save(vm.userId(), vm.roleId()))
        );
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody UserRoleVM vm) {
        userRoleService.delete(vm.userId(), vm.roleId());
        return ResponseEntity.noContent().build();
    }
}
