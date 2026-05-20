package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.PayorPlan;
import com.dazzle.asklepios.domain.PayorPlanItem;
import com.dazzle.asklepios.service.PayorPlanService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
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
import com.dazzle.asklepios.web.rest.vm.payorplan.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class PayorPlanController {

    private static final Logger LOG = LoggerFactory.getLogger(PayorPlanController.class);

    private final PayorPlanService service;

    public PayorPlanController(PayorPlanService service) {
        this.service = service;
    }

    // ---------------- PLAN ----------------

    @PostMapping("/payor-plan")
    public ResponseEntity<com.dazzle.asklepios.web.rest.vm.payorplan.PayorPlanResponseVM> createPlan(@RequestBody PayorPlanSaveVM vm) {
        PayorPlan saved = service.createPlan(vm);
        URI location = URI.create("/api/setup/payor-plan/" + saved.getId());
        return ResponseEntity.created(location).body(PayorPlanResponseVM.ofEntity(saved));
    }

    @PutMapping("/payor-plan")
    public ResponseEntity<PayorPlanResponseVM> updatePlan(@RequestBody PayorPlanUpdateVM vm) {
        PayorPlan saved = service.updatePlan(vm);
        URI location = URI.create("/api/setup/payor-plan/" + saved.getId());
        return ResponseEntity.ok().location(location).body(PayorPlanResponseVM.ofEntity(saved));
    }

    @GetMapping("/payor-plan/{id}")
    public ResponseEntity<PayorPlanResponseVM> getPlan(@PathVariable Long id) {
        return service.findOnePlan(id)
                .map(p -> PayorPlanResponseVM.ofEntity(p))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/payor-plan/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        service.deletePlan(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/payor-plan/{id}/toggle-active")
    public ResponseEntity<PayorPlanResponseVM> togglePlanActive(@PathVariable Long id) {
        return service.togglePlanActive(id)
                .map(p -> PayorPlanResponseVM.ofEntity(p))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // -------- LISTS

    @GetMapping("/payor-plan")
    public ResponseEntity<List<PayorPlanResponseVM>> getAllPlans(@ParameterObject Pageable pageable) {
        Page<PayorPlan> page = service.getAllPlans(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(p -> PayorPlanResponseVM.ofEntity(p)).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/payor-plan/active")
    public ResponseEntity<List<PayorPlanResponseVM>> getAllActivePlans(@ParameterObject Pageable pageable) {
        Page<PayorPlan> page = service.getAllActivePlans(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(p -> PayorPlanResponseVM.ofEntity(p)).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/payor-plan/by-payor/{payorId}")
    public ResponseEntity<List<PayorPlanResponseVM>> getAllByPayor(
            @PathVariable Long payorId,
            @ParameterObject Pageable pageable
    ) {
        Page<PayorPlan> page = service.getAllByPayor(payorId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(p -> PayorPlanResponseVM.ofEntity(p)).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/payor-plan/by-payor/{payorId}/active")
    public ResponseEntity<List<PayorPlanResponseVM>> getAllActiveByPayor(
            @PathVariable Long payorId,
            @ParameterObject Pageable pageable
    ) {
        Page<PayorPlan> page = service.getAllActiveByPayor(payorId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(p -> PayorPlanResponseVM.ofEntity(p)).toList(),
                headers,
                HttpStatus.OK
        );
    }


    // ---------------- ITEMS ----------------

    @PostMapping("/payor-plan-item")
    public ResponseEntity<PayorPlanItemResponseVM> createItem(@RequestBody PayorPlanItemSaveVM vm) {
        PayorPlanItem saved = service.createItem(vm);
        URI location = URI.create("/api/setup/payor-plan-item/" + saved.getId());
        return ResponseEntity.created(location).body(PayorPlanItemResponseVM.ofEntity(saved));
    }

    @PutMapping("/payor-plan-item")
    public ResponseEntity<PayorPlanItemResponseVM> updateItem(@RequestBody PayorPlanItemUpdateVM vm) {
        PayorPlanItem saved = service.updateItem(vm);
        URI location = URI.create("/api/setup/payor-plan-item/" + saved.getId());
        return ResponseEntity.ok().location(location).body(PayorPlanItemResponseVM.ofEntity(saved));
    }

    @DeleteMapping("/payor-plan-item/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        service.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/payor-plan-item/{id}/toggle-active")
    public ResponseEntity<PayorPlanItemResponseVM> toggleItemActive(@PathVariable Long id) {
        return service.toggleItemActive(id)
                .map(PayorPlanItemResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/payor-plan-item/by-plan/{planId}")
    public ResponseEntity<List<PayorPlanItemResponseVM>> getItemsByPlan(
            @PathVariable Long planId,
            @ParameterObject Pageable pageable
    ) {
        Page<PayorPlanItem> page = service.getItemsByPlan(planId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(PayorPlanItemResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/payor-plan-item/by-plan/{planId}/active")
    public ResponseEntity<List<PayorPlanItemResponseVM>> getActiveItemsByPlan(
            @PathVariable Long planId,
            @ParameterObject Pageable pageable
    ) {
        Page<PayorPlanItem> page = service.getActiveItemsByPlan(planId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(PayorPlanItemResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

//    @PostMapping("/payor-plan/cchi/upsert")
//    public ResponseEntity<PayorPlanResponseVM> upsertCchiPayorPlan(@RequestBody CchiPayorPlanUpsertVM vm) {
//        LOG.debug("REST upsert CCHI PayorPlan payload={}", vm);
//
//        PayorPlan saved = service.upsertCchiPayorPlan(vm);
//
//        return ResponseEntity.ok(PayorPlanResponseVM.ofEntity(saved));
//    }
}
