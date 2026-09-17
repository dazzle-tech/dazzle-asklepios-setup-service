package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Payor;
import com.dazzle.asklepios.domain.enumeration.biling.PayorCategory;
import com.dazzle.asklepios.service.PayorService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.payor.PayorResponseVM;
import com.dazzle.asklepios.web.rest.vm.payor.PayorSaveVM;
import com.dazzle.asklepios.web.rest.vm.payor.PayorUpdateVM;
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
public class PayorController {

    private static final Logger LOG = LoggerFactory.getLogger(PayorController.class);

    private final PayorService service;

    public PayorController(PayorService service) {
        this.service = service;
    }

    // -------- CREATE --------
    @PostMapping("/payor")
    public ResponseEntity<PayorResponseVM> create(@RequestBody PayorSaveVM vm) {
        LOG.debug("REST create Payor payload={}", vm);

        Payor saved = service.create(vm);
        PayorResponseVM body = PayorResponseVM.ofEntity(saved);

        URI location = URI.create("/api/setup/payor/" + saved.getId());
        return ResponseEntity.created(location).body(body);
    }

    // -------- UPDATE --------
    @PutMapping("/payor")
    public ResponseEntity<PayorResponseVM> update(@RequestBody PayorUpdateVM vm) {
        LOG.debug("REST update Payor payload={}", vm);

        Payor saved = service.update(vm);
        PayorResponseVM body = PayorResponseVM.ofEntity(saved);

        URI location = URI.create("/api/setup/payor/" + saved.getId());
        return ResponseEntity.ok().location(location).body(body);
    }

    // -------- LIST/SEARCH --------
    @GetMapping("/payor")
    public ResponseEntity<List<PayorResponseVM>> list(
            @RequestParam(required = false) PayorCategory category,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST search Payors category={}, name={}, code={}, page={}",
                category, name, code, pageable);

        Page<Payor> page = service.search(category, name, code, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent().stream().map(PayorResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/payor/{id}")
    public ResponseEntity<PayorResponseVM> get(@PathVariable Long id) {
        LOG.debug("REST get Payor id={}", id);

        return service.findOne(id)
                .map(PayorResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/payor/{id}/toggle-active")
    public ResponseEntity<PayorResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle Payor isActive id={}", id);

        return service.toggleIsActive(id)
                .map(PayorResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/payor/active")
    public ResponseEntity<List<PayorResponseVM>> getAllActive(@ParameterObject Pageable pageable) {
        Page<Payor> page = service.getAllActive(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(PayorResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/payor/cchi/by-nphies/{nphiesId}")
    public ResponseEntity<PayorResponseVM> getByNphiesId(@PathVariable String nphiesId) {
        return service.findByNphiesId(nphiesId)
                .map(PayorResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/payor/ensure-from-nphies/{nphiesId}")
    public ResponseEntity<PayorResponseVM> ensureFromNphies(@PathVariable String nphiesId) {
        LOG.debug("REST ensure Payor from NPHIES id={}", nphiesId);
        Payor payor = service.ensureFromNphiesId(nphiesId);
        return ResponseEntity.ok(PayorResponseVM.ofEntity(payor));
    }
}
