package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.ProcedureItems;
import com.dazzle.asklepios.domain.enumeration.ProcedureItemsType;
import com.dazzle.asklepios.service.ProcedureItemsService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.department.DepartmentResponseVM;
import com.dazzle.asklepios.web.rest.vm.procedureItems.ProcedureItemsCreateVM;
import com.dazzle.asklepios.web.rest.vm.procedureItems.ProcedureItemsResponseVM;
import com.dazzle.asklepios.web.rest.vm.procedureItems.ProcedureItemsUpdateVM;
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
public class ProcedureItemsController {

    private static final Logger LOG = LoggerFactory.getLogger(ProcedureItemsController.class);

    private final ProcedureItemsService procedureItemsService;

    public ProcedureItemsController(ProcedureItemsService procedureItemsService) {
        this.procedureItemsService = procedureItemsService;
    }

    @PostMapping("/procedure-items")
    public ResponseEntity<ProcedureItemsResponseVM> create(@Valid @RequestBody ProcedureItemsCreateVM vm) {
        LOG.debug("REST create ProcedureItems payload={}", vm);

        ProcedureItems created = procedureItemsService.create(vm.procedureId(), vm.toEntity());
        return ResponseEntity
                .created(URI.create("/api/setup/procedure-items/" + created.getId()))
                .body(ProcedureItemsResponseVM.ofEntity(created));
    }

    @PutMapping("/procedure-items/{id}")
    public ResponseEntity<ProcedureItemsResponseVM> update(
            @PathVariable Long id,
            @Valid @RequestBody ProcedureItemsUpdateVM vm
    ) {
        LOG.debug("REST update ProcedureItems id={} payload={}", id, vm);

        return procedureItemsService.update(id, vm.procedureId(), vm.toEntity())
                .map(ProcedureItemsResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/procedure-items/{id}")
    public ResponseEntity<ProcedureItemsResponseVM> get(@PathVariable Long id) {
        LOG.debug("REST get ProcedureItems id={}", id);
        return procedureItemsService.findOne(id)
                .map(ProcedureItemsResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/procedure-items/by-procedure/{procedureId}")
    public ResponseEntity<List<ProcedureItemsResponseVM>> listByProcedure(
            @PathVariable Long procedureId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list ProcedureItems by procedureId={} pageable={}", procedureId, pageable);

        Page<ProcedureItems> page = procedureItemsService.findByProcedureId(procedureId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(ProcedureItemsResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @PatchMapping("/procedure-items/{id}/toggle-active")
    public ResponseEntity<ProcedureItemsResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle ProcedureItems isActive id={}", id);
        return procedureItemsService.toggleIsActive(id)
                .map(ProcedureItemsResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/procedure-items/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete ProcedureItems id={}", id);
        procedureItemsService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/procedure-items/sources/by-facility")
    public ResponseEntity<List<DepartmentResponseVM>> listSourcesByTypeAndFacility(
            @RequestParam ProcedureItemsType type,
            @RequestParam Long facilityId
    ) {
        LOG.debug("REST list procedure item sources by type={} facilityId={}", type, facilityId);

        List<DepartmentResponseVM> response = procedureItemsService
                .findSourcesByTypeAndFacility(type, facilityId)
                .stream()
                .map(DepartmentResponseVM::ofEntity)
                .toList();

        return ResponseEntity.ok(response);
    }
}
