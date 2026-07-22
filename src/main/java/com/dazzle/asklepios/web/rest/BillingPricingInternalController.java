package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.BillingPricingResolutionService;
import com.dazzle.asklepios.service.dto.BillingPricingResolveRequest;
import com.dazzle.asklepios.service.dto.BillingPricingResolveResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class BillingPricingInternalController {

    private static final Logger LOG =
            LoggerFactory.getLogger(
                    BillingPricingInternalController.class
            );

    private final BillingPricingResolutionService
            billingPricingResolutionService;

    @PostMapping("/internal/billing-pricing/resolve")
    public ResponseEntity<BillingPricingResolveResponse>
    resolve(
            @Valid
            @RequestBody
            BillingPricingResolveRequest request
    ) {
        LOG.debug(
                "REST request to resolve billing pricing request={}",
                request
        );

        BillingPricingResolveResponse response =
                billingPricingResolutionService
                        .resolve(request);

        return ResponseEntity.ok(response);
    }
}