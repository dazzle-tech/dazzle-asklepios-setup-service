package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.enumeration.CoverageTermType;
import com.dazzle.asklepios.service.CoverageRuleService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageCopaymentVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageDiscountVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageExclusionVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoveragePreApprovalItemVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoveragePreApprovalVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageTermItemVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageTermVM;
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
@RequestMapping("/api/setup/coverage-contracts")
public class CoverageRuleController {

    private static final Logger LOG = LoggerFactory.getLogger(CoverageRuleController.class);

    private final CoverageRuleService coverageRuleService;

    public CoverageRuleController(CoverageRuleService coverageRuleService) {
        this.coverageRuleService = coverageRuleService;
    }

    @PostMapping("/{contractId:\\d+}/copayments")
    public ResponseEntity<CoverageCopaymentVM> createCopayment(
            @PathVariable Long contractId,
            @Valid @RequestBody CoverageCopaymentVM vm
    ) {
        LOG.debug("REST create copayment contractId={} payload={}", contractId, vm);
        CoverageCopaymentVM body = coverageRuleService.saveCopayment(contractId, vm);
        return ResponseEntity.created(URI.create("/api/setup/coverage-contracts/" + contractId + "/copayments/" + body.id())).body(body);
    }

    @PutMapping("/{contractId:\\d+}/copayments")
    public ResponseEntity<CoverageCopaymentVM> updateCopayment(
            @PathVariable Long contractId,
            @Valid @RequestBody CoverageCopaymentVM vm
    ) {
        return ResponseEntity.ok(coverageRuleService.saveCopayment(contractId, vm));
    }

    @GetMapping("/{contractId:\\d+}/copayments")
    public ResponseEntity<List<CoverageCopaymentVM>> listCopayments(
            @PathVariable Long contractId,
            @RequestParam(required = false, defaultValue = "true") Boolean isActive,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageRuleService.listCopayments(contractId, isActive, pageable));
    }

    @PostMapping("/{contractId:\\d+}/terms")
    public ResponseEntity<CoverageTermVM> createTerm(
            @PathVariable Long contractId,
            @Valid @RequestBody CoverageTermVM vm
    ) {
        CoverageTermVM body = coverageRuleService.saveTerm(contractId, vm);
        return ResponseEntity.created(URI.create("/api/setup/coverage-contracts/" + contractId + "/terms/" + body.id())).body(body);
    }

    @PutMapping("/{contractId:\\d+}/terms")
    public ResponseEntity<CoverageTermVM> updateTerm(
            @PathVariable Long contractId,
            @Valid @RequestBody CoverageTermVM vm
    ) {
        return ResponseEntity.ok(coverageRuleService.saveTerm(contractId, vm));
    }

    @GetMapping("/{contractId:\\d+}/terms")
    public ResponseEntity<List<CoverageTermVM>> listTerms(
            @PathVariable Long contractId,
            @RequestParam CoverageTermType termType,
            @RequestParam(required = false, defaultValue = "true") Boolean isActive,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageRuleService.listTerms(contractId, termType, isActive, pageable));
    }

    @PostMapping("/terms/{termId:\\d+}/items")
    public ResponseEntity<CoverageTermItemVM> createTermItem(
            @PathVariable Long termId,
            @Valid @RequestBody CoverageTermItemVM vm
    ) {
        CoverageTermItemVM body = coverageRuleService.saveTermItem(termId, vm);
        return ResponseEntity.created(URI.create("/api/setup/coverage-contracts/terms/" + termId + "/items/" + body.id())).body(body);
    }

    @PutMapping("/terms/{termId:\\d+}/items")
    public ResponseEntity<CoverageTermItemVM> updateTermItem(
            @PathVariable Long termId,
            @Valid @RequestBody CoverageTermItemVM vm
    ) {
        return ResponseEntity.ok(coverageRuleService.saveTermItem(termId, vm));
    }

    @GetMapping("/terms/{termId:\\d+}/items")
    public ResponseEntity<List<CoverageTermItemVM>> listTermItems(
            @PathVariable Long termId,
            @RequestParam(required = false) Boolean isActive,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageRuleService.listTermItems(termId, isActive, pageable));
    }

    @PostMapping("/{contractId:\\d+}/discounts")
    public ResponseEntity<CoverageDiscountVM> createDiscount(
            @PathVariable Long contractId,
            @Valid @RequestBody CoverageDiscountVM vm
    ) {
        CoverageDiscountVM body = coverageRuleService.createDiscount(contractId, vm);
        return ResponseEntity.created(URI.create("/api/setup/coverage-contracts/" + contractId + "/discounts/" + body.id())).body(body);
    }

    @PatchMapping("/discounts/{id:\\d+}/deactivate")
    public ResponseEntity<CoverageDiscountVM> deactivateDiscount(@PathVariable Long id) {
        return ResponseEntity.ok(coverageRuleService.deactivateDiscount(id));
    }

    @GetMapping("/{contractId:\\d+}/discounts")
    public ResponseEntity<List<CoverageDiscountVM>> listDiscounts(
            @PathVariable Long contractId,
            @RequestParam(required = false) Boolean isActive,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageRuleService.listDiscounts(contractId, isActive, pageable));
    }

    @PostMapping("/{contractId:\\d+}/exclusions")
    public ResponseEntity<CoverageExclusionVM> createExclusion(
            @PathVariable Long contractId,
            @Valid @RequestBody CoverageExclusionVM vm
    ) {
        CoverageExclusionVM body = coverageRuleService.createExclusion(contractId, vm);
        return ResponseEntity.created(URI.create("/api/setup/coverage-contracts/" + contractId + "/exclusions/" + body.id())).body(body);
    }

    @PatchMapping("/exclusions/{id:\\d+}/deactivate")
    public ResponseEntity<CoverageExclusionVM> deactivateExclusion(@PathVariable Long id) {
        return ResponseEntity.ok(coverageRuleService.deactivateExclusion(id));
    }

    @GetMapping("/{contractId:\\d+}/exclusions")
    public ResponseEntity<List<CoverageExclusionVM>> listExclusions(
            @PathVariable Long contractId,
            @RequestParam(required = false) Boolean isActive,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageRuleService.listExclusions(contractId, isActive, pageable));
    }

    @PostMapping("/{contractId:\\d+}/pre-approvals")
    public ResponseEntity<CoveragePreApprovalVM> createPreApproval(
            @PathVariable Long contractId,
            @Valid @RequestBody CoveragePreApprovalVM vm
    ) {
        CoveragePreApprovalVM body = coverageRuleService.savePreApproval(contractId, vm);
        return ResponseEntity.created(URI.create("/api/setup/coverage-contracts/" + contractId + "/pre-approvals/" + body.id())).body(body);
    }

    @PutMapping("/{contractId:\\d+}/pre-approvals")
    public ResponseEntity<CoveragePreApprovalVM> updatePreApproval(
            @PathVariable Long contractId,
            @Valid @RequestBody CoveragePreApprovalVM vm
    ) {
        return ResponseEntity.ok(coverageRuleService.savePreApproval(contractId, vm));
    }

    @GetMapping("/{contractId:\\d+}/pre-approvals")
    public ResponseEntity<List<CoveragePreApprovalVM>> listPreApprovals(
            @PathVariable Long contractId,
            @RequestParam(required = false, defaultValue = "true") Boolean isActive,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageRuleService.listPreApprovals(contractId, isActive, pageable));
    }

    @PostMapping("/pre-approvals/{preApprovalId:\\d+}/items")
    public ResponseEntity<CoveragePreApprovalItemVM> createPreApprovalItem(
            @PathVariable Long preApprovalId,
            @Valid @RequestBody CoveragePreApprovalItemVM vm
    ) {
        CoveragePreApprovalItemVM body = coverageRuleService.createPreApprovalItem(preApprovalId, vm);
        return ResponseEntity.created(URI.create("/api/setup/coverage-contracts/pre-approvals/" + preApprovalId + "/items/" + body.id())).body(body);
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
