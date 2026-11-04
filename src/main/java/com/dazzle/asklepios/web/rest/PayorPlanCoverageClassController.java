package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.PayorPlanCoverageClass;
import com.dazzle.asklepios.service.PayorPlanCoverageClassService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.payorplan.PayorPlanCoverageClassResponseVM;
import com.dazzle.asklepios.web.rest.vm.payorplan.PayorPlanCoverageClassSaveVM;
import com.dazzle.asklepios.web.rest.vm.payorplan.PayorPlanCoverageClassUpdateVM;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class PayorPlanCoverageClassController {

    private static final Logger LOG = LoggerFactory.getLogger(PayorPlanCoverageClassController.class);

    private final PayorPlanCoverageClassService service;

    public PayorPlanCoverageClassController(PayorPlanCoverageClassService service) {
        this.service = service;
    }

    @PostMapping("/payor-plan-coverage-class")
    public ResponseEntity<PayorPlanCoverageClassResponseVM> create(
            @RequestBody PayorPlanCoverageClassSaveVM vm
    ) {
        LOG.debug("REST create PayorPlanCoverageClass payload={}", vm);

        PayorPlanCoverageClass saved = service.create(vm);
        PayorPlanCoverageClassResponseVM body = PayorPlanCoverageClassResponseVM.ofEntity(saved);

        URI location = URI.create("/api/setup/payor-plan-coverage-class/" + saved.getId());
        return ResponseEntity.created(location).body(body);
    }

    @PutMapping("/payor-plan-coverage-class")
    public ResponseEntity<PayorPlanCoverageClassResponseVM> update(
            @RequestBody PayorPlanCoverageClassUpdateVM vm
    ) {
        LOG.debug("REST update PayorPlanCoverageClass payload={}", vm);

        PayorPlanCoverageClass saved = service.update(vm);
        PayorPlanCoverageClassResponseVM body = PayorPlanCoverageClassResponseVM.ofEntity(saved);

        URI location = URI.create("/api/setup/payor-plan-coverage-class/" + saved.getId());
        return ResponseEntity.ok().location(location).body(body);
    }

    @GetMapping("/payor-plan-coverage-class/{id}")
    public ResponseEntity<PayorPlanCoverageClassResponseVM> get(@PathVariable Long id) {
        LOG.debug("REST get PayorPlanCoverageClass id={}", id);

        return service.findOne(id)
                .map(PayorPlanCoverageClassResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/payor-plan/{planId}/coverage-class")
    public ResponseEntity<List<PayorPlanCoverageClassResponseVM>> getByPlan(
            @PathVariable Long planId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST get PayorPlanCoverageClass by planId={}, page={}", planId, pageable);

        Page<PayorPlanCoverageClass> page = service.getByPlan(planId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent().stream()
                        .map(PayorPlanCoverageClassResponseVM::ofEntity)
                        .toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/payor-plan/{planId}/coverage-class/active")
    public ResponseEntity<List<PayorPlanCoverageClassResponseVM>> getActiveByPlan(
            @PathVariable Long planId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST get active PayorPlanCoverageClass by planId={}, page={}", planId, pageable);

        Page<PayorPlanCoverageClass> page = service.getActiveByPlan(planId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent().stream()
                        .map(PayorPlanCoverageClassResponseVM::ofEntity)
                        .toList(),
                headers,
                HttpStatus.OK
        );
    }

    @PatchMapping("/payor-plan-coverage-class/{id}/toggle-active")
    public ResponseEntity<PayorPlanCoverageClassResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle PayorPlanCoverageClass isActive id={}", id);

        return service.toggleIsActive(id)
                .map(PayorPlanCoverageClassResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/payor-plan-coverage-class/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete PayorPlanCoverageClass id={}", id);

        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/payor-plan/{planId}/coverage-class")
    public ResponseEntity<Void> deleteByPlan(@PathVariable Long planId) {
        LOG.debug("REST delete PayorPlanCoverageClass by planId={}", planId);

        service.deleteByPlanId(planId);
        return ResponseEntity.noContent().build();
    }
}