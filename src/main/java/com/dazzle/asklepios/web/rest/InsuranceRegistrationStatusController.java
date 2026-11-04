package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.InsuranceRegistrationStatusService;
import com.dazzle.asklepios.web.rest.vm.insuranceregistration.InsuranceRegistrationStatusVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/setup")
public class InsuranceRegistrationStatusController {

    private static final Logger LOG = LoggerFactory.getLogger(InsuranceRegistrationStatusController.class);

    private final InsuranceRegistrationStatusService insuranceRegistrationStatusService;

    public InsuranceRegistrationStatusController(InsuranceRegistrationStatusService insuranceRegistrationStatusService) {
        this.insuranceRegistrationStatusService = insuranceRegistrationStatusService;
    }

    @GetMapping("/insurance-registration-status")
    public ResponseEntity<InsuranceRegistrationStatusVM> check(
            @RequestParam(required = false) String payerNphiesId,
            @RequestParam(required = false) String policyNumber,
            @RequestParam(required = false) String className
    ) {
        LOG.debug(
                "REST check insurance registration status payerNphiesId={} policyNumber={} className={}",
                payerNphiesId,
                policyNumber,
                className
        );
        return ResponseEntity.ok(insuranceRegistrationStatusService.check(payerNphiesId, policyNumber, className));
    }
}
