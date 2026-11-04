package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.BillingConfiguration;
import com.dazzle.asklepios.domain.enumeration.BillingConfigurationKey;
import com.dazzle.asklepios.domain.enumeration.BillingConfigurationStatus;
import com.dazzle.asklepios.service.BillingConfigurationService;
import com.dazzle.asklepios.service.dto.BillingConfigurationDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class BillingConfigurationController {

    private static final Logger LOG =
            LoggerFactory.getLogger(BillingConfigurationController.class);

    private static final String ENTITY_NAME =
            "billingConfiguration";

    private final BillingConfigurationService billingConfigurationService;

    public BillingConfigurationController(
            BillingConfigurationService billingConfigurationService
    ) {
        this.billingConfigurationService =
                billingConfigurationService;
    }

    @PostMapping("/billing-configuration")
    public ResponseEntity<BillingConfiguration> create(
            @Valid
            @RequestBody
            @NotNull
            BillingConfigurationDTO billingConfigurationDTO
    ) {
        LOG.debug(
                "REST request to create BillingConfiguration payload={}",
                billingConfigurationDTO
        );

        if (billingConfigurationDTO.id() != null) {
            throw new BadRequestAlertException(
                    "A new billing configuration cannot already have an id",
                    ENTITY_NAME,
                    "id.exists"
            );
        }

        BillingConfiguration created =
                billingConfigurationService.create(
                        billingConfigurationDTO
                );

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/setup/billing-configuration/"
                                        + created.getId()
                        )
                )
                .body(created);
    }

    @PutMapping("/billing-configuration/{id}")
    public ResponseEntity<BillingConfiguration> update(
            @PathVariable
            @NotNull
            Long id,

            @Valid
            @RequestBody
            @NotNull
            BillingConfigurationDTO billingConfigurationDTO
    ) {
        LOG.debug(
                "REST request to update BillingConfiguration id={} payload={}",
                id,
                billingConfigurationDTO
        );

        if (billingConfigurationDTO.id() == null) {
            throw new BadRequestAlertException(
                    "Billing configuration id is required",
                    ENTITY_NAME,
                    "id.required"
            );
        }

        if (!id.equals(billingConfigurationDTO.id())) {
            throw new BadRequestAlertException(
                    "Path id and payload id do not match",
                    ENTITY_NAME,
                    "id.mismatch"
            );
        }

        BillingConfiguration updated =
                billingConfigurationService.update(
                        id,
                        billingConfigurationDTO
                );

        return ResponseEntity.ok(updated);
    }

    @PutMapping(
            "/billing-configuration/{id}/activation-status/{active}"
    )
    public ResponseEntity<BillingConfiguration>
    changeActivationStatus(
            @PathVariable("id")
            @NotNull
            Long id,

            @PathVariable("active")
            @NotNull
            Boolean active
    ) {
        LOG.debug(
                "REST request to change BillingConfiguration activation id={} active={}",
                id,
                active
        );

        BillingConfiguration updated =
                billingConfigurationService
                        .changeActivationStatus(
                                id,
                                active
                        );

        return ResponseEntity.ok(updated);
    }

    @PutMapping(
            "/billing-configuration/{id}/status/{status}"
    )
    public ResponseEntity<BillingConfiguration> changeStatus(
            @PathVariable("id")
            @NotNull
            Long id,

            @PathVariable("status")
            @NotNull
            BillingConfigurationStatus status
    ) {
        LOG.debug(
                "REST request to change BillingConfiguration status id={} status={}",
                id,
                status
        );

        BillingConfiguration updated =
                billingConfigurationService.changeStatus(
                        id,
                        status
                );

        return ResponseEntity.ok(updated);
    }

    @GetMapping("/billing-configuration/{id}")
    public ResponseEntity<BillingConfiguration> findById(
            @PathVariable("id")
            @NotNull
            Long id
    ) {
        LOG.debug(
                "REST request to get BillingConfiguration id={}",
                id
        );

        BillingConfiguration billingConfiguration =
                billingConfigurationService.findById(id);

        return ResponseEntity.ok(billingConfiguration);
    }

    @GetMapping(
            "/billing-configuration/search/by-facility/{facilityId}"
    )
    public ResponseEntity<List<BillingConfiguration>>
    findByFacilityId(
            @PathVariable("facilityId")
            @NotNull
            Long facilityId,

            @ParameterObject
            Pageable pageable
    ) {
        LOG.debug(
                "REST request to get BillingConfigurations by facilityId={} pageable={}",
                facilityId,
                pageable
        );

        Page<BillingConfiguration> page =
                billingConfigurationService.findByFacilityId(
                        facilityId,
                        pageable
                );

        HttpHeaders headers =
                PaginationUtil.generatePaginationHttpHeaders(
                        ServletUriComponentsBuilder.fromCurrentRequest(),
                        page
                );

        return new ResponseEntity<>(
                page.getContent(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping(
            "/billing-configuration/search/active/by-facility/{facilityId}"
    )
    public ResponseEntity<List<BillingConfiguration>>
    findActiveByFacilityId(
            @PathVariable("facilityId")
            @NotNull
            Long facilityId,

            @ParameterObject
            Pageable pageable
    ) {
        LOG.debug(
                "REST request to get active BillingConfigurations by facilityId={} pageable={}",
                facilityId,
                pageable
        );

        Page<BillingConfiguration> page =
                billingConfigurationService
                        .findActiveByFacilityId(
                                facilityId,
                                pageable
                        );

        HttpHeaders headers =
                PaginationUtil.generatePaginationHttpHeaders(
                        ServletUriComponentsBuilder.fromCurrentRequest(),
                        page
                );

        return new ResponseEntity<>(
                page.getContent(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping(
            "/billing-configuration/search/by-facility/{facilityId}/status/{status}"
    )
    public ResponseEntity<List<BillingConfiguration>>
    findByFacilityIdAndStatus(
            @PathVariable("facilityId")
            @NotNull
            Long facilityId,

            @PathVariable("status")
            @NotNull
            BillingConfigurationStatus status,

            @ParameterObject
            Pageable pageable
    ) {
        LOG.debug(
                "REST request to get BillingConfigurations by facilityId={} status={} pageable={}",
                facilityId,
                status,
                pageable
        );

        Page<BillingConfiguration> page =
                billingConfigurationService
                        .findByFacilityIdAndStatus(
                                facilityId,
                                status,
                                pageable
                        );

        HttpHeaders headers =
                PaginationUtil.generatePaginationHttpHeaders(
                        ServletUriComponentsBuilder.fromCurrentRequest(),
                        page
                );

        return new ResponseEntity<>(
                page.getContent(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping(
            "/billing-configuration/search/by-facility/{facilityId}/key/{configurationKey}"
    )
    public ResponseEntity<BillingConfiguration>
    findByFacilityIdAndConfigurationKey(
            @PathVariable("facilityId")
            @NotNull
            Long facilityId,

            @PathVariable("configurationKey")
            @NotNull
            BillingConfigurationKey configurationKey
    ) {
        LOG.debug(
                "REST request to get BillingConfiguration by facilityId={} configurationKey={}",
                facilityId,
                configurationKey
        );

        BillingConfiguration billingConfiguration =
                billingConfigurationService
                        .findByFacilityIdAndConfigurationKey(
                                facilityId,
                                configurationKey
                        );

        return ResponseEntity.ok(billingConfiguration);
    }

    @GetMapping(
            "/billing-configuration/search/resolved/by-facility/{facilityId}/key/{configurationKey}"
    )
    public ResponseEntity<Object> getResolvedValue(
            @PathVariable("facilityId")
            @NotNull
            Long facilityId,

            @PathVariable("configurationKey")
            @NotNull
            BillingConfigurationKey configurationKey
    ) {
        LOG.debug(
                "REST request to resolve BillingConfiguration facilityId={} configurationKey={}",
                facilityId,
                configurationKey
        );

        Object resolvedValue =
                billingConfigurationService.getResolvedValue(
                        facilityId,
                        configurationKey
                );

        return ResponseEntity.ok(resolvedValue);
    }

    @DeleteMapping("/billing-configuration/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id")
            @NotNull
            Long id
    ) {
        LOG.debug(
                "REST request to delete BillingConfiguration id={}",
                id
        );

        boolean deleted =
                billingConfigurationService.delete(id);

        if (!deleted) {
            throw new BadRequestAlertException(
                    "Unable to delete billing configuration",
                    ENTITY_NAME,
                    "delete.failed"
            );
        }

        return ResponseEntity.noContent().build();
    }
}