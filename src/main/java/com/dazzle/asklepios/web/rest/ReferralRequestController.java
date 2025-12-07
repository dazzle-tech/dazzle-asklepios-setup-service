package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.ReferralRequest;
import com.dazzle.asklepios.service.ReferralRequestService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.referral.ReferralRequestResponseVM;
import com.dazzle.asklepios.web.rest.vm.referral.ReferralRequestSaveVM;
import com.dazzle.asklepios.web.rest.vm.referral.ReferralRequestUpdateVM;
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
public class ReferralRequestController {

    private static final Logger LOG = LoggerFactory.getLogger(ReferralRequestController.class);

    private final ReferralRequestService service;

    public ReferralRequestController(ReferralRequestService service) {
        this.service = service;
    }

    // -------- CREATE --------
    @PostMapping("/referral-request")
    public ResponseEntity<ReferralRequestResponseVM> create(@RequestBody ReferralRequestSaveVM vm) {
        LOG.debug("REST create ReferralRequest payload={}", vm);

        ReferralRequest saved = service.create(vm);
        ReferralRequestResponseVM body = ReferralRequestResponseVM.ofEntity(saved);

        URI location = URI.create("/api/medical-sheets/referral-request/" + saved.getId());
        return ResponseEntity.created(location).body(body);
    }

    // -------- UPDATE --------
    @PutMapping("/referral-request")
    public ResponseEntity<ReferralRequestResponseVM> update(@RequestBody ReferralRequestUpdateVM vm) {
        LOG.debug("REST update ReferralRequest payload={}", vm);

        ReferralRequest saved = service.update(vm);
        ReferralRequestResponseVM body = ReferralRequestResponseVM.ofEntity(saved);

        URI location = URI.create("/api/medical-sheets/referral-request/" + saved.getId());
        return ResponseEntity.ok().location(location).body(body);
    }
    @GetMapping("/referral-request")
    public ResponseEntity<List<ReferralRequestResponseVM>> list(
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list ReferralRequests page={}", pageable);

        Page<ReferralRequest> page = service.findAll(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                page.getContent().stream().map(ReferralRequestResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    // -------- GET BY ID --------
    @GetMapping("/referral-request/{id}")
    public ResponseEntity<ReferralRequestResponseVM> get(@PathVariable Long id) {
        LOG.debug("REST get ReferralRequest id={}", id);

        return service.findOne(id)
                .map(ReferralRequestResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // -------- GET BY PATIENT --------
    @GetMapping("/referral-request/by-patient/{patientId}")
    public ResponseEntity<List<ReferralRequestResponseVM>> getByPatient(
            @PathVariable Long patientId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST get ReferralRequests by patientId={}, page={}", patientId, pageable);

        Page<ReferralRequest> page = service.getByPatient(patientId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent().stream().map(ReferralRequestResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    // -------- GET BY PATIENT + ENCOUNTER --------
    @GetMapping("/referral-request/by-patient/{patientId}/encounter/{encounterId}")
    public ResponseEntity<List<ReferralRequestResponseVM>> getByPatientAndEncounter(
            @PathVariable Long patientId,
            @PathVariable Long encounterId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST get ReferralRequests by patientId={} encounterId={}, page={}",
                patientId, encounterId, pageable);

        Page<ReferralRequest> page = service.getByPatientAndEncounter(patientId, encounterId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent().stream().map(ReferralRequestResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }
}
