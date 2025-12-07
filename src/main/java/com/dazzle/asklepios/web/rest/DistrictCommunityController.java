package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DistrictCommunity;
import com.dazzle.asklepios.service.DistrictCommunityService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.districtCommunity.DistrictCommunityCreateVM;
import com.dazzle.asklepios.web.rest.vm.districtCommunity.DistrictCommunityResponseVM;
import com.dazzle.asklepios.web.rest.vm.districtCommunity.DistrictCommunityUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class DistrictCommunityController {

    private static final Logger LOG = LoggerFactory.getLogger(DistrictCommunityController.class);

    private final DistrictCommunityService communityService;

    public DistrictCommunityController(DistrictCommunityService communityService) {
        this.communityService = communityService;
    }

    @PostMapping("/district/{districtId}/community")
    public ResponseEntity<DistrictCommunityResponseVM> createCommunity(
            @PathVariable Long districtId,
            @Valid @RequestBody DistrictCommunityCreateVM vm
    ) {
        LOG.debug("REST create DistrictCommunity districtId={} payload={}", districtId, vm);

        DistrictCommunity incoming = DistrictCommunity.builder()
                .name(vm.name())
                .isActive(vm.isActive() != null ? vm.isActive() : Boolean.TRUE)
                .build();

        DistrictCommunity created = communityService.create(districtId, incoming);
        DistrictCommunityResponseVM body = DistrictCommunityResponseVM.ofEntity(created);

        return ResponseEntity
                .created(URI.create("/api/setup/district/" + districtId + "/community/" + created.getId()))
                .body(body);
    }

    @PutMapping("/district/{districtId}/community/{id}")
    public ResponseEntity<DistrictCommunityResponseVM> updateCommunity(
            @PathVariable Long districtId,
            @PathVariable Long id,
            @Valid @RequestBody DistrictCommunityUpdateVM vm
    ) {
        LOG.debug("REST update DistrictCommunity id={} payload={}", id, vm);

        DistrictCommunity patch = new DistrictCommunity();
        patch.setName(vm.name());
        patch.setIsActive(vm.isActive());

        return communityService.update(id, patch)
                .map(DistrictCommunityResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/community")
    public ResponseEntity<List<DistrictCommunityResponseVM>> getAllCommunitiesPaged(
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list DistrictCommunities pageable={}", pageable);

        Page<DistrictCommunity> page = communityService.findAll(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<DistrictCommunityResponseVM> body = page.getContent()
                .stream()
                .map(DistrictCommunityResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/district/{districtId}/community")
    public ResponseEntity<List<DistrictCommunityResponseVM>> getByDistrict(
            @PathVariable Long districtId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list communities by districtId={} pageable={}", districtId, pageable);

        Page<DistrictCommunity> page = communityService.findByDistrict(districtId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<DistrictCommunityResponseVM> body = page.getContent()
                .stream()
                .map(DistrictCommunityResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/district/{districtId}/community/by-name")
    public ResponseEntity<List<DistrictCommunityResponseVM>> getByName(
            @PathVariable Long districtId,
            @RequestParam(required = false) String name,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list communities by name='{}' for districtId={} pageable={}", name, districtId, pageable);

        Page<DistrictCommunity> page = communityService.findByName(districtId, name, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<DistrictCommunityResponseVM> body = page.getContent()
                .stream()
                .map(DistrictCommunityResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @PatchMapping("/community/{id}/toggle-active")
    public ResponseEntity<DistrictCommunityResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle DistrictCommunity isActive id={}", id);

        return communityService.toggleIsActive(id)
                .map(DistrictCommunityResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
