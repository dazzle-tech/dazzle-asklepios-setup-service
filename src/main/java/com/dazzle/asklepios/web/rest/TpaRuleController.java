package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.CoverageRuleService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageDiscountVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageExclusionVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoveragePreApprovalItemVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoveragePreApprovalVM;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup/tpa-definitions")
public class TpaRuleController {

    private static final Logger LOG = LoggerFactory.getLogger(TpaRuleController.class);

    private final CoverageRuleService coverageRuleService;

    public TpaRuleController(CoverageRuleService coverageRuleService) {
        this.coverageRuleService = coverageRuleService;
    }

    @PostMapping("/{tpaId:\\d+}/discounts")
    public ResponseEntity<CoverageDiscountVM> createDiscount(
            @PathVariable Long tpaId,
            @Valid @RequestBody CoverageDiscountVM vm
    ) {
        LOG.debug("REST create TPA discount tpaId={} payload={}", tpaId, vm);
        CoverageDiscountVM body = coverageRuleService.createTpaDiscount(tpaId, vm);
        return ResponseEntity.created(URI.create("/api/setup/tpa-definitions/" + tpaId + "/discounts/" + body.id())).body(body);
    }

    @PatchMapping("/discounts/{id:\\d+}/deactivate")
    public ResponseEntity<CoverageDiscountVM> deactivateDiscount(@PathVariable Long id) {
        return ResponseEntity.ok(coverageRuleService.deactivateDiscount(id));
    }

    @GetMapping("/{tpaId:\\d+}/discounts")
    public ResponseEntity<List<CoverageDiscountVM>> listDiscounts(
            @PathVariable Long tpaId,
            @RequestParam(required = false) Boolean isActive,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageRuleService.listTpaDiscounts(tpaId, isActive, pageable));
    }

    @PostMapping("/{tpaId:\\d+}/exclusions")
    public ResponseEntity<CoverageExclusionVM> createExclusion(
            @PathVariable Long tpaId,
            @Valid @RequestBody CoverageExclusionVM vm
    ) {
        LOG.debug("REST create TPA exclusion tpaId={} payload={}", tpaId, vm);
        CoverageExclusionVM body = coverageRuleService.createTpaExclusion(tpaId, vm);
        return ResponseEntity.created(URI.create("/api/setup/tpa-definitions/" + tpaId + "/exclusions/" + body.id())).body(body);
    }

    @PatchMapping("/exclusions/{id:\\d+}/deactivate")
    public ResponseEntity<CoverageExclusionVM> deactivateExclusion(@PathVariable Long id) {
        return ResponseEntity.ok(coverageRuleService.deactivateExclusion(id));
    }

    @GetMapping("/{tpaId:\\d+}/exclusions")
    public ResponseEntity<List<CoverageExclusionVM>> listExclusions(
            @PathVariable Long tpaId,
            @RequestParam(required = false) Boolean isActive,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageRuleService.listTpaExclusions(tpaId, isActive, pageable));
    }

    @PostMapping("/{tpaId:\\d+}/pre-approvals")
    public ResponseEntity<CoveragePreApprovalVM> createPreApproval(
            @PathVariable Long tpaId,
            @Valid @RequestBody CoveragePreApprovalVM vm
    ) {
        CoveragePreApprovalVM body = coverageRuleService.saveTpaPreApproval(tpaId, vm);
        return ResponseEntity.created(URI.create("/api/setup/tpa-definitions/" + tpaId + "/pre-approvals/" + body.id())).body(body);
    }

    @PutMapping("/{tpaId:\\d+}/pre-approvals")
    public ResponseEntity<CoveragePreApprovalVM> updatePreApproval(
            @PathVariable Long tpaId,
            @Valid @RequestBody CoveragePreApprovalVM vm
    ) {
        return ResponseEntity.ok(coverageRuleService.saveTpaPreApproval(tpaId, vm));
    }

    @GetMapping("/{tpaId:\\d+}/pre-approvals")
    public ResponseEntity<List<CoveragePreApprovalVM>> listPreApprovals(
            @PathVariable Long tpaId,
            @RequestParam(required = false, defaultValue = "true") Boolean isActive,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageRuleService.listTpaPreApprovals(tpaId, isActive, pageable));
    }

    @PostMapping("/pre-approvals/{preApprovalId:\\d+}/items")
    public ResponseEntity<CoveragePreApprovalItemVM> createPreApprovalItem(
            @PathVariable Long preApprovalId,
            @Valid @RequestBody CoveragePreApprovalItemVM vm
    ) {
        CoveragePreApprovalItemVM body = coverageRuleService.createPreApprovalItem(preApprovalId, vm);
        return ResponseEntity.created(URI.create("/api/setup/tpa-definitions/pre-approvals/" + preApprovalId + "/items/" + body.id())).body(body);
    }

    @PatchMapping("/pre-approval-items/{id:\\d+}/deactivate")
    public ResponseEntity<CoveragePreApprovalItemVM> deactivatePreApprovalItem(@PathVariable Long id) {
        return ResponseEntity.ok(coverageRuleService.deactivatePreApprovalItem(id));
    }

    @GetMapping("/pre-approvals/{preApprovalId:\\d+}/items")
    public ResponseEntity<List<CoveragePreApprovalItemVM>> listPreApprovalItems(
            @PathVariable Long preApprovalId,
            @RequestParam(required = false, defaultValue = "true") Boolean isActive,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageRuleService.listPreApprovalItems(preApprovalId, isActive, pageable));
    }

    private <T> ResponseEntity<List<T>> paged(Page<T> page) {
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
