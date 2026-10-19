package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.InsuranceCompanyPolicy;
import com.dazzle.asklepios.service.InsuranceCompanyPolicyService;
import com.dazzle.asklepios.web.rest.vm.insurancecompanypolicy.InsuranceCompanyPolicyResponseVM;
import com.dazzle.asklepios.web.rest.vm.insurancecompanypolicy.InsuranceCompanyPolicySaveVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class InsuranceCompanyPolicyController {

    private static final Logger LOG = LoggerFactory.getLogger(InsuranceCompanyPolicyController.class);

    private final InsuranceCompanyPolicyService insuranceCompanyPolicyService;

    public InsuranceCompanyPolicyController(InsuranceCompanyPolicyService insuranceCompanyPolicyService) {
        this.insuranceCompanyPolicyService = insuranceCompanyPolicyService;
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

    @GetMapping("/nphies-payers/{insuranceCompanyId:\\d+}/policies")
    public ResponseEntity<List<InsuranceCompanyPolicyResponseVM>> list(@PathVariable Long insuranceCompanyId) {
        LOG.debug("REST list insurance company policies insuranceCompanyId={}", insuranceCompanyId);

        List<InsuranceCompanyPolicyResponseVM> policies = insuranceCompanyPolicyService
                .findByInsuranceCompanyId(insuranceCompanyId)
                .stream()
                .map(InsuranceCompanyPolicyResponseVM::ofEntity)
                .toList();
        return ResponseEntity.ok(policies);
    }
}
