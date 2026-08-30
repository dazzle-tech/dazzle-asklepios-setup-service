package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.TpaDefinition;
import com.dazzle.asklepios.service.TpaDefinitionService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.tpadefinition.TpaDefinitionResponseVM;
import com.dazzle.asklepios.web.rest.vm.tpadefinition.TpaDefinitionSaveVM;
import com.dazzle.asklepios.web.rest.vm.tpadefinition.TpaDefinitionUpdateVM;
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
public class TpaDefinitionController {

    private static final Logger LOG = LoggerFactory.getLogger(TpaDefinitionController.class);

    private final TpaDefinitionService tpaDefinitionService;

    public TpaDefinitionController(TpaDefinitionService tpaDefinitionService) {
        this.tpaDefinitionService = tpaDefinitionService;
    }

    @PostMapping("/tpa-definitions")
    public ResponseEntity<TpaDefinitionResponseVM> create(@Valid @RequestBody TpaDefinitionSaveVM vm) {
        LOG.debug("REST create TPA payload={}", vm);

        TpaDefinition saved = tpaDefinitionService.create(vm);
        URI location = URI.create("/api/setup/tpa-definitions/" + saved.getId());
        return ResponseEntity.created(location).body(TpaDefinitionResponseVM.ofEntity(saved));
    }

    @PutMapping("/tpa-definitions")
    public ResponseEntity<TpaDefinitionResponseVM> update(@Valid @RequestBody TpaDefinitionUpdateVM vm) {
        LOG.debug("REST update TPA payload={}", vm);

        TpaDefinition saved = tpaDefinitionService.update(vm);
        URI location = URI.create("/api/setup/tpa-definitions/" + saved.getId());
        return ResponseEntity.ok().location(location).body(TpaDefinitionResponseVM.ofEntity(saved));
    }

    @GetMapping("/tpa-definitions/{id:\\d+}")
    public ResponseEntity<TpaDefinitionResponseVM> get(@PathVariable Long id) {
        LOG.debug("REST get TPA id={}", id);

        return tpaDefinitionService.findOne(id)
                .map(TpaDefinitionResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/tpa-definitions/{id:\\d+}/toggle-active")
    public ResponseEntity<TpaDefinitionResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle TPA isActive id={}", id);

        return tpaDefinitionService.toggleIsActive(id)
                .map(TpaDefinitionResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/tpa-definitions")
    public ResponseEntity<List<TpaDefinitionResponseVM>> getAll(@ParameterObject Pageable pageable) {
        LOG.debug("REST list ALL TPAs pageable={}", pageable);
        return pagedResponse(tpaDefinitionService.findAll(pageable));
    }

    @GetMapping("/tpa-definitions/active")
    public ResponseEntity<List<TpaDefinitionResponseVM>> getActive(@ParameterObject Pageable pageable) {
        LOG.debug("REST list active TPAs pageable={}", pageable);
        return pagedResponse(tpaDefinitionService.findActive(pageable));
    }

    @GetMapping("/tpa-definitions/by-code/{tpaCode}")
    public ResponseEntity<List<TpaDefinitionResponseVM>> getByCode(
            @PathVariable String tpaCode,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list TPAs by tpaCode='{}' pageable={}", tpaCode, pageable);
        return pagedResponse(tpaDefinitionService.findByTpaCode(tpaCode, pageable));
    }

    @GetMapping("/tpa-definitions/by-name/{name}")
    public ResponseEntity<List<TpaDefinitionResponseVM>> getByName(
            @PathVariable String name,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list TPAs by name='{}' pageable={}", name, pageable);
        return pagedResponse(tpaDefinitionService.findByName(name, pageable));
    }

    @GetMapping("/tpa-definitions/linkable-insurance-companies")
    public ResponseEntity<List<TpaDefinitionResponseVM.TpaLinkedInsuranceCompanyVM>> getLinkableInsuranceCompanies() {
        LOG.debug("REST list insurance companies available to link to a TPA");
        return ResponseEntity.ok(
                tpaDefinitionService.findLinkableInsuranceCompanies().stream()
                        .map(TpaDefinitionResponseVM.TpaLinkedInsuranceCompanyVM::ofEntity)
                        .toList()
        );
    }

    @GetMapping("/tpa-definitions/{id:\\d+}/insurance-companies")
    public ResponseEntity<List<TpaDefinitionResponseVM.TpaLinkedInsuranceCompanyVM>> getLinkedInsuranceCompanies(
            @PathVariable Long id
    ) {
        LOG.debug("REST list insurance companies linked to TPA id={}", id);
        return ResponseEntity.ok(
                tpaDefinitionService.findLinkedInsuranceCompanies(id).stream()
                        .map(TpaDefinitionResponseVM.TpaLinkedInsuranceCompanyVM::ofEntity)
                        .toList()
        );
    }

    private ResponseEntity<List<TpaDefinitionResponseVM>> pagedResponse(Page<TpaDefinitionResponseVM> page) {
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent(),
                headers,
                HttpStatus.OK
        );
    }
}
