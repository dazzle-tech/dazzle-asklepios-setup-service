package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.enumeration.ConfigurationScope;
import com.dazzle.asklepios.service.ChildGrowthSetupService;
import com.dazzle.asklepios.web.rest.vm.childgrowth.ChildGrowthSetupResponseVM;
import com.dazzle.asklepios.web.rest.vm.childgrowth.ChildGrowthSetupSaveVM;
import com.dazzle.asklepios.web.rest.vm.childgrowth.ChildGrowthUserOptionVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Child growth chart setup: which chart applies by sex and age, and how results are banded.
 */
@RestController
@RequestMapping("/api/setup")
public class ChildGrowthSetupController {

    private static final Logger LOG = LoggerFactory.getLogger(ChildGrowthSetupController.class);

    private final ChildGrowthSetupService service;

    public ChildGrowthSetupController(ChildGrowthSetupService service) {
        this.service = service;
    }

    @GetMapping("/child-growth-setup/users")
    public ResponseEntity<List<ChildGrowthUserOptionVM>> users(@RequestParam Long facilityId) {
        LOG.debug("REST list active child growth users facilityId={}", facilityId);
        return ResponseEntity.ok(service.activeUsers(facilityId));
    }

    @GetMapping("/child-growth-setup")
    public ResponseEntity<ChildGrowthSetupResponseVM> get(
            @RequestParam ConfigurationScope scope,
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) List<Long> userIds
    ) {
        LOG.debug("REST get ChildGrowthSetup scope={} facilityId={} userIds={}", scope, facilityId, userIds);
        return ResponseEntity.ok(service.get(scope, facilityId, userIds));
    }

    @PutMapping("/child-growth-setup")
    public ResponseEntity<ChildGrowthSetupResponseVM> save(@Valid @RequestBody ChildGrowthSetupSaveVM vm) {
        LOG.debug("REST save ChildGrowthSetup scope={} facilityId={} userIds={}",
                vm.scope(), vm.facilityId(), vm.userIds());
        return ResponseEntity.ok(service.save(vm));
    }
}
