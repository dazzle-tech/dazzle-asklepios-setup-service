package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.PayorPlan;
import com.dazzle.asklepios.service.PayorPlanService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.payorPlan.PayorPlanResponseVM;
import com.dazzle.asklepios.web.rest.vm.payorPlan.PayorPlanSaveVM;
import com.dazzle.asklepios.web.rest.vm.payorPlan.PayorPlanUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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

    @PostMapping("/payor-plan")
    public ResponseEntity<PayorPlanResponseVM> create(@RequestBody PayorPlanSaveVM vm) {
        PayorPlan saved = service.create(vm);
        URI location = URI.create("/api/setup/payor-plan/" + saved.getId());
        return ResponseEntity.created(location).body(PayorPlanResponseVM.ofEntity(saved));
    }

    @PutMapping("/payor-plan")
    public ResponseEntity<PayorPlanResponseVM> update(@RequestBody PayorPlanUpdateVM vm) {
        PayorPlan saved = service.update(vm);
        URI location = URI.create("/api/setup/payor-plan/" + saved.getId());
        return ResponseEntity.ok().location(location).body(PayorPlanResponseVM.ofEntity(saved));
    }

    @GetMapping("/payor-plan/by-payor/{payorId}")
    public ResponseEntity<List<PayorPlanResponseVM>> getByPayor(
            @PathVariable Long payorId,
            @ParameterObject Pageable pageable
    ) {
        Page<PayorPlan> page = service.getByPayor(payorId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                page.getContent().stream().map(PayorPlanResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/payor-plan/{id}")
    public ResponseEntity<PayorPlanResponseVM> get(@PathVariable Long id) {
        return service.findOne(id)
                .map(PayorPlanResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/payor-plan/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
