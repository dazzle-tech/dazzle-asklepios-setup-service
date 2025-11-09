package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.AgeGroup;
import com.dazzle.asklepios.domain.enumeration.AgeGroupType;
import com.dazzle.asklepios.service.AgeGroupService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.ageGroup.AgeGroupCreateVM;
import com.dazzle.asklepios.web.rest.vm.ageGroup.AgeGroupResponseVM;
import com.dazzle.asklepios.web.rest.vm.ageGroup.AgeGroupUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class AgeGroupController {

    private static final Logger LOG = LoggerFactory.getLogger(AgeGroupController.class);
    private final AgeGroupService ageGroupService;

    public AgeGroupController(AgeGroupService ageGroupService) {
        this.ageGroupService = ageGroupService;
    }

    // ====================== CREATE ======================
    @PostMapping("/age-group")
    public ResponseEntity<AgeGroupResponseVM> createAgeGroup(
            @RequestParam Long facilityId,
            @Valid @RequestBody AgeGroupCreateVM vm
    ) {
        LOG.debug("REST create AgeGroup facilityId={} payload={}", facilityId, vm);

        AgeGroup toCreate = AgeGroup.builder()
                .ageGroup(vm.ageGroup())
                .fromAge(vm.fromAge())
                .toAge(vm.toAge())
                .fromAgeUnit(vm.fromAgeUnit())
                .toAgeUnit(vm.toAgeUnit())
                .build();

        AgeGroup created = ageGroupService.create(facilityId, toCreate);
        AgeGroupResponseVM body = AgeGroupResponseVM.ofEntity(created);

        return ResponseEntity
                .created(URI.create("/api/setup/age-group/" + created.getId() + "?facilityId=" + facilityId))
                .body(body);
    }

    // ====================== UPDATE ======================
    @PutMapping("/age-group/{id}")
    public ResponseEntity<AgeGroupResponseVM> updateAgeGroup(
            @PathVariable Long id,
            @RequestParam Long facilityId,
            @Valid @RequestBody AgeGroupUpdateVM vm
    ) {
        LOG.debug("REST update AgeGroup id={} facilityId={} payload={}", id, facilityId, vm);

        AgeGroup patch = new AgeGroup();
        patch.setAgeGroup(vm.ageGroup());
        patch.setFromAge(vm.fromAge());
        patch.setToAge(vm.toAge());
        patch.setFromAgeUnit(vm.fromAgeUnit());
        patch.setToAgeUnit(vm.toAgeUnit());
        patch.setLastModifiedBy(vm.lastModifiedBy());

        return ageGroupService.update(id, facilityId, patch)
                .map(AgeGroupResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ====================== READ ALL ======================
    @GetMapping("/age-group")
    public ResponseEntity<List<AgeGroupResponseVM>> getAllAgeGroups(
            @RequestParam Long facilityId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list AgeGroups facilityId={} pageable={}", facilityId, pageable);
        final Page<AgeGroup> page = ageGroupService.findAll(facilityId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(
                page.getContent().stream().map(AgeGroupResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    // ====================== FILTERS ======================
    @GetMapping("/age-group/by-label/{label}")
    public ResponseEntity<List<AgeGroupResponseVM>> getByAgeGroupLabel(
            @PathVariable("label") AgeGroupType label,
            @RequestParam Long facilityId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list AgeGroups by label='{}' facilityId={} pageable={}", label, facilityId, pageable);
        Page<AgeGroup> page = ageGroupService.findByAgeGroup(facilityId, label, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(
                page.getContent().stream().map(AgeGroupResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/age-group/by-from-age/{fromAge}")
    public ResponseEntity<List<AgeGroupResponseVM>> getByFromAge(
            @PathVariable BigDecimal fromAge,
            @RequestParam Long facilityId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list AgeGroups by fromAge='{}' facilityId={} pageable={}", fromAge, facilityId, pageable);
        Page<AgeGroup> page = ageGroupService.findByFromAge(facilityId, fromAge, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(
                page.getContent().stream().map(AgeGroupResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/age-group/by-to-age/{toAge}")
    public ResponseEntity<List<AgeGroupResponseVM>> getByToAge(
            @PathVariable BigDecimal toAge,
            @RequestParam Long facilityId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list AgeGroups by toAge='{}' facilityId={} pageable={}", toAge, facilityId, pageable);
        Page<AgeGroup> page = ageGroupService.findByToAge(facilityId, toAge, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(
                page.getContent().stream().map(AgeGroupResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }
    @DeleteMapping("/age-group/{id}")
    public ResponseEntity<Void> deleteAgeGroup(@PathVariable Long id) {
        LOG.debug("REST request to delete AgeGroup id={}", id);

        boolean deleted = ageGroupService.delete(id);

        if (deleted) {
            LOG.info("Successfully deleted AgeGroup id={}", id);
            return ResponseEntity.noContent().build(); 
        } else {
            LOG.warn("Failed to delete AgeGroup id={}, not found or constraint violation", id);
            return ResponseEntity.notFound().build();
        }
    }


}
