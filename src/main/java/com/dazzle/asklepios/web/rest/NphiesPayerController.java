package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.service.NphiesPayerService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.NphiesPayerResponseVM;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.NphiesPayerSaveVM;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.NphiesPayerTpasUpdateVM;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.NphiesPayerUpdateVM;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class NphiesPayerController {

    private static final Logger LOG = LoggerFactory.getLogger(NphiesPayerController.class);

    private final NphiesPayerService nphiesPayerService;

    public NphiesPayerController(NphiesPayerService nphiesPayerService) {
        this.nphiesPayerService = nphiesPayerService;
    }

    @PostMapping("/nphies-payers")
    public ResponseEntity<NphiesPayerResponseVM> create(@Valid @RequestBody NphiesPayerSaveVM vm) {
        LOG.debug("REST create NPHIES Payer payload={}", vm);

        NphiesPayer saved = nphiesPayerService.create(vm);
        URI location = URI.create("/api/setup/nphies-payers/" + saved.getId());
        return ResponseEntity.created(location).body(NphiesPayerResponseVM.ofEntity(saved));
    }

    @PutMapping("/nphies-payers")
    public ResponseEntity<NphiesPayerResponseVM> update(@Valid @RequestBody NphiesPayerUpdateVM vm) {
        LOG.debug("REST update NPHIES Payer payload={}", vm);

        NphiesPayer saved = nphiesPayerService.update(vm);
        URI location = URI.create("/api/setup/nphies-payers/" + saved.getId());
        return ResponseEntity.ok().location(location).body(NphiesPayerResponseVM.ofEntity(saved));
    }

    @GetMapping("/nphies-payers/{id:\\d+}")
    public ResponseEntity<NphiesPayerResponseVM> get(@PathVariable Long id) {
        LOG.debug("REST get NPHIES Payer id={}", id);

        return nphiesPayerService.findOne(id)
                .map(NphiesPayerResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/nphies-payers/{id:\\d+}/toggle-active")
    public ResponseEntity<NphiesPayerResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle NPHIES Payer isActive id={}", id);

        return nphiesPayerService.toggleIsActive(id)
                .map(NphiesPayerResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/nphies-payers/{id:\\d+}/tpas")
    public ResponseEntity<NphiesPayerResponseVM> updateTpas(
            @PathVariable Long id,
            @RequestBody NphiesPayerTpasUpdateVM vm
    ) {
        LOG.debug("REST update NPHIES Payer TPA links id={} payload={}", id, vm);
        NphiesPayer saved = nphiesPayerService.updateTpas(id, vm == null ? List.of() : vm.tpaIds());
        return ResponseEntity.ok(NphiesPayerResponseVM.ofEntity(saved));
    }

    @GetMapping("/nphies-payers/{id:\\d+}/available-tpas")
    public ResponseEntity<List<NphiesPayerResponseVM.LinkedTpaVM>> getAvailableTpas(@PathVariable Long id) {
        LOG.debug("REST list available TPAs for NPHIES Payer id={}", id);
        return ResponseEntity.ok(
                nphiesPayerService.findAvailableTpas(id).stream()
                        .map(NphiesPayerResponseVM.LinkedTpaVM::ofEntity)
                        .toList()
        );
    }

    @GetMapping("/nphies-payers/active")
    public ResponseEntity<List<NphiesPayerResponseVM>> getActive() {
        LOG.debug("REST list active NPHIES Payers");
        return ResponseEntity.ok(
                nphiesPayerService.findActive().stream()
                        .map(NphiesPayerResponseVM::ofEntity)
                        .toList()
        );
    }

    @GetMapping("/nphies-payers")
    public ResponseEntity<List<NphiesPayerResponseVM>> getAllNphiesPayers(
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list ALL NPHIES Payers pageable={}", pageable);

        Page<NphiesPayer> page = nphiesPayerService.findAll(pageable);
        return pagedResponse(page);
    }

    @GetMapping("/nphies-payers/by-nphies-id/{nphiesId}")
    public ResponseEntity<List<NphiesPayerResponseVM>> getByNphiesId(
            @PathVariable String nphiesId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug(
                "REST list NPHIES Payers by nphiesId='{}' pageable={}",
                nphiesId,
                pageable
        );

        Page<NphiesPayer> page = nphiesPayerService.findByNphiesId(nphiesId, pageable);
        return pagedResponse(page);
    }

    @GetMapping("/nphies-payers/by-name-en/{nameEn}")
    public ResponseEntity<List<NphiesPayerResponseVM>> getByNameEn(
            @PathVariable String nameEn,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug(
                "REST list NPHIES Payers by nameEn='{}' pageable={}",
                nameEn,
                pageable
        );

        Page<NphiesPayer> page = nphiesPayerService.findByNameEn(nameEn, pageable);
        return pagedResponse(page);
    }

    @GetMapping("/nphies-payers/by-name-ar/{nameAr}")
    public ResponseEntity<List<NphiesPayerResponseVM>> getByNameAr(
            @PathVariable String nameAr,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug(
                "REST list NPHIES Payers by nameAr='{}' pageable={}",
                nameAr,
                pageable
        );

        Page<NphiesPayer> page = nphiesPayerService.findByNameAr(nameAr, pageable);
        return pagedResponse(page);
    }

    private ResponseEntity<List<NphiesPayerResponseVM>> pagedResponse(Page<NphiesPayer> page) {
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent().stream().map(NphiesPayerResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/nphies-payers/{id}")
    public ResponseEntity<NphiesPayer> getById(@PathVariable Long id) {
        LOG.debug("REST get NPHIES Payer id={}", id);

        return nphiesPayerService.findOne(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
