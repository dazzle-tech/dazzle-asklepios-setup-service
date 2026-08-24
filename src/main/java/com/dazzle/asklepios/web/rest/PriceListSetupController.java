package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.PriceListSetupService;
import com.dazzle.asklepios.service.dto.BillingPricingResolutionDTO;
import com.dazzle.asklepios.service.dto.BillingPricingResolutionRequest;
import com.dazzle.asklepios.service.dto.PriceListSetupCloneRequest;
import com.dazzle.asklepios.service.dto.PriceListSetupDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

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

    @PostMapping("/price-list-setups/{id}/clone")
    public ResponseEntity<PriceListSetupDTO> clonePriceListSetup(
            @PathVariable Long id,
            @Valid @RequestBody PriceListSetupCloneRequest request
    ) {
        PriceListSetupDTO result =
                priceListSetupService.clonePriceList(id, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }

    @GetMapping("/price-list-setups/resolve")
    public ResponseEntity<BillingPricingResolutionDTO> resolve(
            @Valid BillingPricingResolutionRequest request
    ) {
        BillingPricingResolutionDTO result =
                priceListSetupService.resolve(request);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/price-list-setups/requires-preauth")
    public ResponseEntity<Boolean> requiresPreAuthorization(
            @Valid BillingPricingResolutionRequest request
    ) {
        return ResponseEntity.ok(
                priceListSetupService.requiresPreAuthorization(
                        request
                )
        );
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
    public ResponseEntity<List<PriceListSetupDTO>>
    getAllPriceListSetups(
            @ParameterObject Pageable pageable
    ) {
        Page<PriceListSetupDTO> page =
                priceListSetupService.findAll(pageable);

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

    @GetMapping("/price-list-setups/by-loggedIn-facility")
    public ResponseEntity<List<PriceListSetupDTO>>
    getAllPriceListSetupsBasedOnLoggedInFacility(
            @ParameterObject Pageable pageable
    ) {
        Page<PriceListSetupDTO> page =
                priceListSetupService.findAllBasedOnLoggedInFacility(pageable);

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

}