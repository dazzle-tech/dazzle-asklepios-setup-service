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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class AgeGroupController {

    private static final Logger LOG = LoggerFactory.getLogger(AgeGroupController.class);
    private final AgeGroupService ageGroupService;

    public AgeGroupController(AgeGroupService ageGroupService) {
        this.ageGroupService = ageGroupService;
    }


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


    @GetMapping("/age-group")
    public ResponseEntity<List<AgeGroupResponseVM>> getAllAgeGroups(
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list ALL AgeGroups (paged, no facility filter)");
        final Page<AgeGroup> page = ageGroupService.findAll(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(AgeGroupResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }


    @GetMapping("/age-group/by-facility")
    public ResponseEntity<List<AgeGroupResponseVM>> getByFacility(
            @RequestParam Long facilityId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list AgeGroups by facilityId={} pageable={}", facilityId, pageable);

        final Page<AgeGroup> page = ageGroupService.findByFacility(facilityId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(AgeGroupResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }


    @GetMapping("/age-group/by-label/{label}")
    public ResponseEntity<List<AgeGroupResponseVM>> getByAgeGroupLabel(
            @PathVariable("label") AgeGroupType label,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list AgeGroups by label='{}' (no facility filter)", label);

        Page<AgeGroup> page = ageGroupService.findByAgeGroup(label, pageable);

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
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list AgeGroups by fromAge='{}'", fromAge);

        Page<AgeGroup> page = ageGroupService.findByFromAge(fromAge, pageable);

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
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list AgeGroups by toAge='{}'", toAge);

        Page<AgeGroup> page = ageGroupService.findByToAge(toAge, pageable);

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
            LOG.warn("Failed to delete AgeGroup id={}", id);
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/age-group/by-birthdate")
    public ResponseEntity<AgeGroupResponseVM> getAgeGroupByBirthDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate
    ) {
        LOG.debug("REST get AgeGroup by birthDate={}", birthDate);

        AgeGroup result = ageGroupService.findAgeGroupByBirthDate(birthDate);

        if (result == null) {
            LOG.warn("No matching AgeGroup found for birthDate={}", birthDate);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(AgeGroupResponseVM.ofEntity(result));
    }

}
