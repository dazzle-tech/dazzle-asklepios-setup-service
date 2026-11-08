package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.InsuranceCompanyPolicy;
import com.dazzle.asklepios.service.CoverageClassService;
import com.dazzle.asklepios.service.InsuranceCompanyPolicyService;
import com.dazzle.asklepios.web.rest.vm.ToggleActiveVM;
import com.dazzle.asklepios.web.rest.vm.coverage.PolicyCoverageClassResponseVM;
import com.dazzle.asklepios.web.rest.vm.insurancecompanypolicy.InsuranceCompanyPolicyResponseVM;
import com.dazzle.asklepios.web.rest.vm.insurancecompanypolicy.InsuranceCompanyPolicySaveVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class InsuranceCompanyPolicyController {

    private static final Logger LOG = LoggerFactory.getLogger(InsuranceCompanyPolicyController.class);

    private final InsuranceCompanyPolicyService insuranceCompanyPolicyService;
    private final CoverageClassService coverageClassService;

    public InsuranceCompanyPolicyController(
            InsuranceCompanyPolicyService insuranceCompanyPolicyService,
            CoverageClassService coverageClassService
    ) {
        this.insuranceCompanyPolicyService = insuranceCompanyPolicyService;
        this.coverageClassService = coverageClassService;
    }

    @PostMapping("/nphies-payers/{insuranceCompanyId:\\d+}/policies")
    public ResponseEntity<InsuranceCompanyPolicyResponseVM> create(
            @PathVariable Long insuranceCompanyId,
            @Valid @RequestBody InsuranceCompanyPolicySaveVM vm
    ) {
        LOG.debug("REST create insurance company policy insuranceCompanyId={} payload={}", insuranceCompanyId, vm);

        InsuranceCompanyPolicy created = insuranceCompanyPolicyService.create(insuranceCompanyId, vm);
        URI location = URI.create("/api/setup/nphies-payers/" + insuranceCompanyId + "/policies/" + created.getId());
        return ResponseEntity.created(location).body(InsuranceCompanyPolicyResponseVM.ofEntity(created));
    }

    @PutMapping("/nphies-payers/{insuranceCompanyId:\\d+}/policies/{policyId:\\d+}")
    public ResponseEntity<InsuranceCompanyPolicyResponseVM> update(
            @PathVariable Long insuranceCompanyId,
            @PathVariable Long policyId,
            @Valid @RequestBody InsuranceCompanyPolicySaveVM vm
    ) {
        LOG.debug("REST update insurance company policy insuranceCompanyId={} policyId={} payload={}", insuranceCompanyId, policyId, vm);
        InsuranceCompanyPolicy updated = insuranceCompanyPolicyService.update(insuranceCompanyId, policyId, vm);
        return ResponseEntity.ok(InsuranceCompanyPolicyResponseVM.ofEntity(updated));
    }

    @PatchMapping("/nphies-payers/{insuranceCompanyId:\\d+}/policies/{policyId:\\d+}/toggle-active")
    public ResponseEntity<InsuranceCompanyPolicyResponseVM> toggleActive(
            @PathVariable Long insuranceCompanyId,
            @PathVariable Long policyId,
            @Valid @RequestBody(required = false) ToggleActiveVM vm
    ) {
        LOG.debug("REST toggle insurance company policy insuranceCompanyId={} policyId={}", insuranceCompanyId, policyId);
        InsuranceCompanyPolicy updated = insuranceCompanyPolicyService.toggleActive(
                insuranceCompanyId,
                policyId,
                ToggleActiveVM.reasonOf(vm)
        );
        return ResponseEntity.ok(InsuranceCompanyPolicyResponseVM.ofEntity(updated));
    }

    @GetMapping("/nphies-payers/{insuranceCompanyId:\\d+}/policies")
    public ResponseEntity<List<InsuranceCompanyPolicyResponseVM>> list(
            @PathVariable Long insuranceCompanyId,
            @RequestParam(required = false) String policyName,
            @RequestParam(required = false) String policyNumber,
            @RequestParam(required = false) String policyCode
    ) {
        LOG.debug("REST list insurance company policies insuranceCompanyId={}", insuranceCompanyId);

        List<InsuranceCompanyPolicyResponseVM> policies = insuranceCompanyPolicyService
                .search(insuranceCompanyId, policyName, policyNumber, policyCode)
                .stream()
                .map(InsuranceCompanyPolicyResponseVM::ofEntity)
                .toList();
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/nphies-payers/{insuranceCompanyId:\\d+}/policies/{policyId:\\d+}/classes")
    public ResponseEntity<List<PolicyCoverageClassResponseVM>> listClasses(
            @PathVariable Long insuranceCompanyId,
            @PathVariable Long policyId
    ) {
        LOG.debug(
                "REST list coverage classes for policy insuranceCompanyId={} policyId={}",
                insuranceCompanyId,
                policyId
        );
        return ResponseEntity.ok(coverageClassService.listByPolicy(insuranceCompanyId, policyId));
    }
}
