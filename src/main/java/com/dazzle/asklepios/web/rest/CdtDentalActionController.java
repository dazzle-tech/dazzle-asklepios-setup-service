package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.CdtDentalAction;
import com.dazzle.asklepios.service.CdtDentalActionService;
import com.dazzle.asklepios.web.rest.vm.cdt.CdtDentalActionCreateVM;
import com.dazzle.asklepios.web.rest.vm.cdt.CdtDentalActionResponseVM;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class CdtDentalActionController {

    private final CdtDentalActionService service;
    private static final Logger LOG = LoggerFactory.getLogger(CdtDentalActionController.class);

    @PostMapping("/cdt-dental-action")
    public ResponseEntity<CdtDentalActionResponseVM> create(
            @Valid @RequestBody CdtDentalActionCreateVM vm) {

        LOG.debug("REST request to create CdtDentalAction: {}", vm);

        CdtDentalAction result =
                service.create(vm.dentalActionId(), vm.cdtCode());

        return ResponseEntity
                .created(URI.create("/api/setup/cdt-dental-action/" + result.getId()))
                .body(CdtDentalActionResponseVM.ofEntity(result));
    }

    @GetMapping("/cdt-dental-action/by-dental-action/{dentalActionId}")
    public List<CdtDentalActionResponseVM> findByDentalAction(@PathVariable Long dentalActionId) {
        return service.findByDentalActionId(dentalActionId)
                .stream().map(CdtDentalActionResponseVM::ofEntity).toList();
    }

    @GetMapping("/cdt-dental-action/by-code/{cdtCode}")
    public List<CdtDentalActionResponseVM> findByCode(@PathVariable String cdtCode) {
        return service.findByCdtCode(cdtCode)
                .stream().map(CdtDentalActionResponseVM::ofEntity).toList();
    }

    @DeleteMapping("/cdt-dental-action/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
