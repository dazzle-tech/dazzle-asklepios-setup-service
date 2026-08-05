package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.PriceListSetupService;
import com.dazzle.asklepios.service.dto.BillingPricingResolutionDTO;
import com.dazzle.asklepios.service.dto.BillingPricingResolutionRequest;
import com.dazzle.asklepios.service.dto.PriceListSetupDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class PriceListSetupController {

    private final PriceListSetupService priceListSetupService;

    @PostMapping("/price-list-setups")
    public ResponseEntity<PriceListSetupDTO> createPriceListSetup(
            @Valid @RequestBody PriceListSetupDTO dto
    ) {
        PriceListSetupDTO result =
                priceListSetupService.create(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }

    @PutMapping("/price-list-setups/{id}")
    public ResponseEntity<PriceListSetupDTO> updatePriceListSetup(
            @PathVariable Long id,
            @Valid @RequestBody PriceListSetupDTO dto
    ) {
        PriceListSetupDTO result =
                priceListSetupService.update(id, dto);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/price-list-setups/{id}")
    public ResponseEntity<PriceListSetupDTO> getPriceListSetup(
            @PathVariable Long id
    ) {
        PriceListSetupDTO result =
                priceListSetupService.findById(id);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/price-list-setups")
    public ResponseEntity<Page<PriceListSetupDTO>>
    getAllPriceListSetups(Pageable pageable) {

        Page<PriceListSetupDTO> result =
                priceListSetupService.findAll(pageable);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/price-list-setups/{id}")
    public ResponseEntity<Void> deletePriceListSetup(
            @PathVariable Long id
    ) {
        priceListSetupService.delete(id);

        return ResponseEntity.noContent().build();
    }


    @PutMapping("/price-list-setups/{id}/activate")
    public ResponseEntity<PriceListSetupDTO> activatePriceListSetup(
            @PathVariable Long id
    ) {
        PriceListSetupDTO result =
                priceListSetupService.activate(id);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/price-list-setups/resolve")
    public ResponseEntity<BillingPricingResolutionDTO> resolve(
            @Valid BillingPricingResolutionRequest request
    ) {
        BillingPricingResolutionDTO result =
                priceListSetupService.resolve(request);

        return ResponseEntity.ok(result);
    }
}