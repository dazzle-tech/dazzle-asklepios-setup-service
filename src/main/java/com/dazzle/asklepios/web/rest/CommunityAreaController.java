package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.CommunityArea;
import com.dazzle.asklepios.service.CommunityAreaService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.communityArea.CommunityAreaCreateVM;
import com.dazzle.asklepios.web.rest.vm.communityArea.CommunityAreaResponseVM;
import com.dazzle.asklepios.web.rest.vm.communityArea.CommunityAreaUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class CommunityAreaController {

    private static final Logger LOG = LoggerFactory.getLogger(CommunityAreaController.class);

    private final CommunityAreaService areaService;

    public CommunityAreaController(CommunityAreaService areaService) {
        this.areaService = areaService;
    }

    @PostMapping("/district/{districtId}/community-area")
    public ResponseEntity<CommunityAreaResponseVM> createArea(
            @PathVariable Long districtId,
            @Valid @RequestBody CommunityAreaCreateVM vm
    ) {
        CommunityArea incoming = CommunityArea.builder()
                .name(vm.name())
                .isActive(vm.isActive() != null ? vm.isActive() : Boolean.TRUE)
                .build();

        CommunityArea created = areaService.create(districtId, incoming);
        CommunityAreaResponseVM body = CommunityAreaResponseVM.ofEntity(created);

        return ResponseEntity
                .created(URI.create("/api/setup/district/" + districtId + "/community-area/" + created.getId()))
                .body(body);
    }

    @PutMapping("/district/{districtId}/community-area/{id}")
    public ResponseEntity<CommunityAreaResponseVM> updateArea(
            @PathVariable Long districtId,
            @PathVariable Long id,
            @Valid @RequestBody CommunityAreaUpdateVM vm
    ) {
        CommunityArea patch = new CommunityArea();
        patch.setName(vm.name());
        patch.setIsActive(vm.isActive());

        return areaService.update(id, patch)
                .map(CommunityAreaResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/community-area")
    public ResponseEntity<List<CommunityAreaResponseVM>> getAllAreasPaged(@ParameterObject Pageable pageable) {
        Page<CommunityArea> page = areaService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        List<CommunityAreaResponseVM> body = page.getContent().stream().map(CommunityAreaResponseVM::ofEntity).toList();
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/district/{districtId}/community-area")
    public ResponseEntity<List<CommunityAreaResponseVM>> getByDistrict(@PathVariable Long districtId, @ParameterObject Pageable pageable) {
        Page<CommunityArea> page = areaService.findByCommunity(districtId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        List<CommunityAreaResponseVM> body = page.getContent().stream().map(CommunityAreaResponseVM::ofEntity).toList();
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/district/{districtId}/community-area/by-name")
    public ResponseEntity<List<CommunityAreaResponseVM>> getByName(
            @PathVariable Long districtId,
            @RequestParam(required = false) String name,
            @ParameterObject Pageable pageable
    ) {
        Page<CommunityArea> page = areaService.findByName(districtId, name, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        List<CommunityAreaResponseVM> body = page.getContent().stream().map(CommunityAreaResponseVM::ofEntity).toList();
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @PatchMapping("/community-area/{id}/toggle-active")
    public ResponseEntity<CommunityAreaResponseVM> toggleActive(@PathVariable Long id) {
        return areaService.toggleIsActive(id)
                .map(CommunityAreaResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
