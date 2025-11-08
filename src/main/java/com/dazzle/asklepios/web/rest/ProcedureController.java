package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.enumeration.ProcedureCategoryType;
import com.dazzle.asklepios.service.ProcedureService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.procedure.ProcedureCreateVM;
import com.dazzle.asklepios.web.rest.vm.procedure.ProcedureResponseVM;
import com.dazzle.asklepios.web.rest.vm.procedure.ProcedureUpdateVM;
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
public class ProcedureController {

    private static final Logger LOG = LoggerFactory.getLogger(ProcedureController.class);

    private final ProcedureService procedureService;

    public ProcedureController(ProcedureService procedureService) {
        this.procedureService = procedureService;
    }

    // ====================== CREATE ======================
    @PostMapping("/procedure")
    public ResponseEntity<ProcedureResponseVM> createProcedure(
            @RequestParam Long facilityId,
            @Valid @RequestBody ProcedureCreateVM vm
    ) {
        LOG.debug("REST create Procedure facilityId={} payload={}", facilityId, vm);

        Procedure toCreate = Procedure.builder()
                .name(vm.name())
                .code(vm.code())
                .categoryType(vm.categoryType())
                .isAppointable(Boolean.TRUE.equals(vm.isAppointable()))
                .indications(vm.indications())
                .contraindications(vm.contraindications())
                .preparationInstructions(vm.preparationInstructions())
                .recoveryNotes(vm.recoveryNotes())
                .isActive(Boolean.TRUE.equals(vm.isActive()))
                .build();

        Procedure created = procedureService.create(facilityId, toCreate);
        ProcedureResponseVM body = ProcedureResponseVM.ofEntity(created);

        return ResponseEntity
                .created(URI.create("/api/setup/procedure/" + created.getId()))
                .body(body);
    }

    // ====================== UPDATE ======================
    @PutMapping("/procedure/{id}")
    public ResponseEntity<ProcedureResponseVM> updateProcedure(
            @PathVariable Long id,
            @RequestParam Long facilityId,
            @Valid @RequestBody ProcedureUpdateVM vm
    ) {
        LOG.debug("REST update Procedure id={} facilityId={} payload={}", id, facilityId, vm);

        Procedure patch = new Procedure();
        patch.setName(vm.name());
        patch.setCode(vm.code());
        patch.setCategoryType(vm.categoryType());
        patch.setIsAppointable(vm.isAppointable());
        patch.setIndications(vm.indications());
        patch.setContraindications(vm.contraindications());
        patch.setPreparationInstructions(vm.preparationInstructions());
        patch.setRecoveryNotes(vm.recoveryNotes());
        patch.setIsActive(vm.isActive());
        patch.setLastModifiedBy(vm.lastModifiedBy());

        return procedureService.update(id, facilityId, patch)
                .map(ProcedureResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ====================== READ ALL (بدون facilityId) ======================
    @GetMapping("/procedure")
    public ResponseEntity<List<ProcedureResponseVM>> getAllProcedures(
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Procedures pageable={}", pageable);
        final Page<Procedure> page = procedureService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(ProcedureResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    // ====================== FILTERS (بدون facilityId) ======================
    @GetMapping("/procedure/by-category/{categoryType}")
    public ResponseEntity<List<ProcedureResponseVM>> getByCategory(
            @PathVariable ProcedureCategoryType categoryType,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Procedures by categoryType={} pageable={}", categoryType, pageable);
        Page<Procedure> page = procedureService.findByCategory(categoryType, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(ProcedureResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/procedure/by-code/{code}")
    public ResponseEntity<List<ProcedureResponseVM>> getByCode(
            @PathVariable String code,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Procedures by code='{}' pageable={}", code, pageable);
        Page<Procedure> page = procedureService.findByCodeContainingIgnoreCase(code, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(ProcedureResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/procedure/by-name/{name}")
    public ResponseEntity<List<ProcedureResponseVM>> getByName(
            @PathVariable String name,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Procedures by name='{}' pageable={}", name, pageable);
        Page<Procedure> page = procedureService.findByNameContainingIgnoreCase(name, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );
        return new ResponseEntity<>(
                page.getContent().stream().map(ProcedureResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    // ====================== TOGGLE ACTIVE (بدون facilityId) ======================
    @PatchMapping("/procedure/{id}/toggle-active")
    public ResponseEntity<ProcedureResponseVM> toggleProcedureActiveStatus(
            @PathVariable Long id
    ) {
        LOG.debug("REST toggle Procedure isActive id={}", id);
        return procedureService.toggleIsActive(id)
                .map(ProcedureResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    // ====================== FILTER by Facility (paged) ======================
    @GetMapping("/procedure/by-facility/{facilityId}")
    public ResponseEntity<List<ProcedureResponseVM>> getByFacility(
            @PathVariable Long facilityId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list Procedures by facilityId={} pageable={}", facilityId, pageable);
        Page<Procedure> page = procedureService.findByFacility(facilityId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                page.getContent().stream().map(ProcedureResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

}
