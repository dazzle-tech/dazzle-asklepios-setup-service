package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.BillingRule;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.service.BillingRuleService;
import com.dazzle.asklepios.service.dto.BillingRuleDTO;
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
public class BillingRuleController {

    private static final Logger LOG =
            LoggerFactory.getLogger(
                    BillingRuleController.class
            );

    private static final String ENTITY_NAME =
            "billingRule";

    private final BillingRuleService billingRuleService;

    public BillingRuleController(
            BillingRuleService billingRuleService
    ) {
        this.billingRuleService = billingRuleService;
    }

    @PostMapping("/billing-rule")
    public ResponseEntity<BillingRule> create(
            @Valid
            @RequestBody
            @NotNull
            BillingRuleDTO billingRuleDTO
    ) {
        LOG.debug(
                "REST request to create BillingRule payload={}",
                billingRuleDTO
        );

        if (billingRuleDTO.id() != null) {
            throw new BadRequestAlertException(
                    "A new billing rule cannot already have an id",
                    ENTITY_NAME,
                    "id.exists"
            );
        }

        BillingRule created =
                billingRuleService.create(
                        billingRuleDTO
                );

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/setup/billing-rule/"
                                        + created.getId()
                        )
                )
                .body(created);
    }

    @PutMapping("/billing-rule/{id}")
    public ResponseEntity<BillingRule> update(
            @PathVariable("id")
            @NotNull
            Long id,

            @Valid
            @RequestBody
            @NotNull
            BillingRuleDTO billingRuleDTO
    ) {
        LOG.debug(
                "REST request to update BillingRule id={} payload={}",
                id,
                billingRuleDTO
        );

        if (billingRuleDTO.id() == null) {
            throw new BadRequestAlertException(
                    "Billing rule id is required",
                    ENTITY_NAME,
                    "id.required"
            );
        }

        if (!id.equals(billingRuleDTO.id())) {
            throw new BadRequestAlertException(
                    "Path id and payload id do not match",
                    ENTITY_NAME,
                    "id.mismatch"
            );
        }

        BillingRule updated =
                billingRuleService.update(
                        id,
                        billingRuleDTO
                );

        return ResponseEntity.ok(updated);
    }

    @GetMapping("/billing-rule/{id}")
    public ResponseEntity<BillingRule> getById(
            @PathVariable("id")
            @NotNull Long id ) {
        LOG.debug(
                "REST request to get BillingRule id={}",
                id
        );

        BillingRule billingRule =
                billingRuleService.getById(id);

        return ResponseEntity.ok(billingRule);
    }

    @GetMapping("/billing-rule")
    public ResponseEntity<List<BillingRule>> findAll(
            @ParameterObject
            Pageable pageable
    ) {
        LOG.debug(
                "REST request to get BillingRules pageable={}",
                pageable
        );

        Page<BillingRule> page =
                billingRuleService.findAll(pageable);

        HttpHeaders headers =
                PaginationUtil.generatePaginationHttpHeaders(
                        ServletUriComponentsBuilder
                                .fromCurrentRequest(),
                        page
                );

        return new ResponseEntity<>(
                page.getContent(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping(
            "/billing-rule/search/by-item-type/{billingItemType}"
    )
    public ResponseEntity<List<BillingRule>>
    findByBillingItemType(
            @PathVariable("billingItemType")
            @NotNull
            BillingItemTypes billingItemType,

            @ParameterObject
            Pageable pageable
    ) {
        LOG.debug(
                "REST request to get BillingRules by itemType={} pageable={}",
                billingItemType,
                pageable
        );

        Page<BillingRule> page =
                billingRuleService
                        .findByBillingItemType(
                                billingItemType,
                                pageable
                        );

        HttpHeaders headers =
                PaginationUtil.generatePaginationHttpHeaders(
                        ServletUriComponentsBuilder
                                .fromCurrentRequest(),
                        page
                );

        return new ResponseEntity<>(
                page.getContent(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping(
            "/billing-rule/default/{billingItemType}"
    )
    public ResponseEntity<BillingRule> getDefault(
            @PathVariable("billingItemType")
            @NotNull
            BillingItemTypes billingItemType
    ) {
        LOG.debug(
                "REST request to get default BillingRule itemType={}",
                billingItemType
        );

        BillingRule billingRule =
                billingRuleService.getDefault(
                        billingItemType
                );

        return ResponseEntity.ok(billingRule);
    }

    @GetMapping("/billing-rule/default")
    public ResponseEntity<BillingRule> getGlobalDefault() {
        LOG.debug(
                "REST request to get global default BillingRule"
        );

        BillingRule billingRule =
                billingRuleService.getGlobalDefault();

        return ResponseEntity.ok(billingRule);
    }

    @PutMapping("/billing-rule/{id}/set-default")
    public ResponseEntity<BillingRule> setDefault(
            @PathVariable("id")
            @NotNull
            Long id
    ) {
        LOG.debug(
                "REST request to set BillingRule as default id={}",
                id
        );

        BillingRule billingRule =
                billingRuleService.setDefault(id);

        return ResponseEntity.ok(billingRule);
    }

    @DeleteMapping("/billing-rule/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id")
            @NotNull
            Long id
    ) {
        LOG.debug(
                "REST request to delete BillingRule id={}",
                id
        );

        boolean deleted =
                billingRuleService.delete(id);

        if (!deleted) {
            throw new BadRequestAlertException(
                    "Billing rule was not found or could not be deleted",
                    ENTITY_NAME,
                    "delete.failed"
            );
        }

        return ResponseEntity
                .noContent()
                .build();
    }
}