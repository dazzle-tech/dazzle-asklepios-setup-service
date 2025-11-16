package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.ProcedurePriceList;
import com.dazzle.asklepios.service.ProcedurePriceListService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.procedurePriceList.ProcedurePriceListCreateVM;
import com.dazzle.asklepios.web.rest.vm.procedurePriceList.ProcedurePriceListResponseVM;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class ProcedurePriceListController {

    private static final Logger LOG = LoggerFactory.getLogger(ProcedurePriceListController.class);

    private final ProcedurePriceListService service;

    public ProcedurePriceListController(ProcedurePriceListService service) {
        this.service = service;
    }

    @PostMapping("/procedure-price-list")
    public ResponseEntity<ProcedurePriceListResponseVM> create(
            @RequestParam Long procedureId,
            @Valid @RequestBody ProcedurePriceListCreateVM vm
    ) {
        LOG.debug("REST create ProcedurePriceList procedureId={} payload={}", procedureId, vm);

        ProcedurePriceList toCreate = ProcedurePriceList.builder()
                .price(vm.price())
                .currency(vm.currency())
                .build();

        ProcedurePriceList created = service.create(procedureId, toCreate);
        ProcedurePriceListResponseVM body = ProcedurePriceListResponseVM.ofEntity(created);

        return ResponseEntity
                .created(URI.create("/api/setup/procedure-price-list/" + created.getId()))
                .body(body);
    }

    @GetMapping("/procedure-price-list/by-procedure/{procedureId}")
    public ResponseEntity<List<ProcedurePriceListResponseVM>> getByProcedureId(
            @PathVariable Long procedureId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list ProcedurePriceList by procedureId={} pageable={}", procedureId, pageable);
        Page<ProcedurePriceList> page = service.findByProcedureId(procedureId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                page.getContent().stream().map(ProcedurePriceListResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @DeleteMapping("/procedure-price-list/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete ProcedurePriceList id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
