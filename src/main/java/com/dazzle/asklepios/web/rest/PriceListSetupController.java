package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupStatus;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupType;
import com.dazzle.asklepios.service.PriceListSetupService;
import com.dazzle.asklepios.service.dto.BillingPricingResolutionDTO;
import com.dazzle.asklepios.service.dto.BillingPricingResolutionRequest;
import com.dazzle.asklepios.service.dto.PriceListSetupDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
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

    @GetMapping("/price-list-setups/by-facility/{facilityId}")
    public ResponseEntity<List<PriceListSetupDTO>> getPriceListSetupsByFacility(
            @PathVariable Long facilityId,
            @ParameterObject Pageable pageable
    ) {
        return pagedResponse(priceListSetupService.findByFacilityId(facilityId, pageable));
    }

    @GetMapping("/price-list-setups/by-name/{name}")
    public ResponseEntity<List<PriceListSetupDTO>> getPriceListSetupsByName(
            @PathVariable String name,
            @ParameterObject Pageable pageable
    ) {
        return pagedResponse(priceListSetupService.findByName(name.trim(), pageable));
    }

    @GetMapping("/price-list-setups/by-type/{type}")
    public ResponseEntity<List<PriceListSetupDTO>> getPriceListSetupsByType(
            @PathVariable PriceListSetupType type,
            @ParameterObject Pageable pageable
    ) {
        return pagedResponse(priceListSetupService.findByType(type, pageable));
    }

    @GetMapping("/price-list-setups/by-payer/{payerId}")
    public ResponseEntity<List<PriceListSetupDTO>> getPriceListSetupsByPayer(
            @PathVariable Long payerId,
            @ParameterObject Pageable pageable
    ) {
        return pagedResponse(priceListSetupService.findByPayerId(payerId, pageable));
    }

    @GetMapping("/price-list-setups/by-version/{versionNumber}")
    public ResponseEntity<List<PriceListSetupDTO>> getPriceListSetupsByVersion(
            @PathVariable Integer versionNumber,
            @ParameterObject Pageable pageable
    ) {
        return pagedResponse(priceListSetupService.findByVersionNumber(versionNumber, pageable));
    }

    @GetMapping("/price-list-setups/by-effective-from/{effectiveFrom}")
    public ResponseEntity<List<PriceListSetupDTO>> getPriceListSetupsByEffectiveFrom(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom,
            @ParameterObject Pageable pageable
    ) {
        return pagedResponse(priceListSetupService.findByEffectiveFrom(effectiveFrom, pageable));
    }

    @GetMapping("/price-list-setups/by-effective-to/{effectiveTo}")
    public ResponseEntity<List<PriceListSetupDTO>> getPriceListSetupsByEffectiveTo(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveTo,
            @ParameterObject Pageable pageable
    ) {
        return pagedResponse(priceListSetupService.findByEffectiveTo(effectiveTo, pageable));
    }

    @GetMapping("/price-list-setups/by-currency/{currency}")
    public ResponseEntity<List<PriceListSetupDTO>> getPriceListSetupsByCurrency(
            @PathVariable Currency currency,
            @ParameterObject Pageable pageable
    ) {
        return pagedResponse(priceListSetupService.findByCurrency(currency, pageable));
    }

    @GetMapping("/price-list-setups/by-status/{status}")
    public ResponseEntity<List<PriceListSetupDTO>> getPriceListSetupsByStatus(
            @PathVariable PriceListSetupStatus status,
            @ParameterObject Pageable pageable
    ) {
        return pagedResponse(priceListSetupService.findByStatus(status, pageable));
    }

    private ResponseEntity<List<PriceListSetupDTO>> pagedResponse(Page<PriceListSetupDTO> page) {
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