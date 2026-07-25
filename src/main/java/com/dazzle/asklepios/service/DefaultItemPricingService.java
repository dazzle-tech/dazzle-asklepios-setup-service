package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.repository.BrandMedicationRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.ProcedureRepository;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultItemPricingService {

    private static final String ENTITY_NAME =
            "defaultItemPricing";

    private final ServiceRepository serviceRepository;

    private final ProcedureRepository procedureRepository;

    private final DiagnosticTestRepository diagnosticTestRepository;

    private final BrandMedicationRepository brandMedicationRepository;

    public DefaultItemPrice resolve(
            BillingItemTypes billingItemType,
            Long sourceId,
            Currency requestedCurrency
    ) {
        if (billingItemType == null) {
            throw new BadRequestAlertException(
                    "Billing item type is required.",
                    ENTITY_NAME,
                    "billingItemType.required"
            );
        }

        if (sourceId == null) {
            throw new BadRequestAlertException(
                    "Source ID is required.",
                    ENTITY_NAME,
                    "sourceId.required"
            );
        }

        return switch (billingItemType) {

            case SERVICE ->
                    resolveService(
                            sourceId,
                            requestedCurrency
                    );

            case PROCEDURE ->
                    resolveProcedure(
                            sourceId,
                            requestedCurrency
                    );

            case LABORATORY,
                 RADIOLOGY,
                 PATHOLOGY ->
                    resolveDiagnosticTest(
                            sourceId,
                            requestedCurrency
                    );

            case MEDICATION ->
                    resolveMedication(
                            sourceId,
                            requestedCurrency
                    );
        };
    }

    private DefaultItemPrice resolveService(
            Long sourceId,
            Currency requestedCurrency
    ) {
        ServiceSetup service =
                serviceRepository
                        .findById(sourceId)
                        .orElseThrow(() ->
                                notFound(
                                        "Service",
                                        sourceId
                                )
                        );

        validateActive(
                service.getIsActive(),
                "Service",
                sourceId
        );

        Currency currency =
                validateCurrency(
                        service.getCurrency(),
                        requestedCurrency,
                        "Service",
                        sourceId
                );

        return new DefaultItemPrice(
                service.getId(),
                service.getCode(),
                service.getName(),
                validatePrice(
                        service.getPrice(),
                        "Service",
                        sourceId
                ),
                currency
        );
    }

    private DefaultItemPrice resolveProcedure(
            Long sourceId,
            Currency requestedCurrency
    ) {
        Procedure procedure =
                procedureRepository
                        .findById(sourceId)
                        .orElseThrow(() ->
                                notFound(
                                        "Procedure",
                                        sourceId
                                )
                        );

        validateActive(
                procedure.getIsActive(),
                "Procedure",
                sourceId
        );

        Currency currency =
                validateCurrency(
                        procedure.getCurrency(),
                        requestedCurrency,
                        "Procedure",
                        sourceId
                );

        BigDecimal price =
                procedure.getPrice() == null
                        ? null
                        : BigDecimal.valueOf(
                        procedure.getPrice()
                );

        return new DefaultItemPrice(
                procedure.getId(),
                procedure.getCode(),
                procedure.getName(),
                validatePrice(
                        price,
                        "Procedure",
                        sourceId
                ),
                currency
        );
    }

    private DefaultItemPrice resolveDiagnosticTest(
            Long sourceId,
            Currency requestedCurrency
    ) {
        DiagnosticTest diagnosticTest =
                diagnosticTestRepository
                        .findById(sourceId)
                        .orElseThrow(() ->
                                notFound(
                                        "Diagnostic test",
                                        sourceId
                                )
                        );

        validateActive(
                diagnosticTest.getIsActive(),
                "Diagnostic test",
                sourceId
        );

        Currency currency =
                validateCurrency(
                        diagnosticTest.getCurrency(),
                        requestedCurrency,
                        "Diagnostic test",
                        sourceId
                );

        String itemCode =
                diagnosticTest.getInternalCode();

        return new DefaultItemPrice(
                diagnosticTest.getId(),
                itemCode,
                diagnosticTest.getName(),
                validatePrice(
                        diagnosticTest.getPrice(),
                        "Diagnostic test",
                        sourceId
                ),
                currency
        );
    }

    private DefaultItemPrice resolveMedication(
            Long sourceId,
            Currency requestedCurrency
    ) {
        BrandMedication medication =
                brandMedicationRepository
                        .findById(sourceId)
                        .orElseThrow(() ->
                                notFound(
                                        "Brand medication",
                                        sourceId
                                )
                        );

        validateActive(
                medication.getIsActive(),
                "Brand medication",
                sourceId
        );

        Currency medicationCurrency =
                parseCurrency(
                        medication.getCurrency(),
                        "Brand medication",
                        sourceId
                );

        Currency currency =
                validateCurrency(
                        medicationCurrency,
                        requestedCurrency,
                        "Brand medication",
                        sourceId
                );

        return new DefaultItemPrice(
                medication.getId(),
                medication.getCode(),
                medication.getName(),
                validatePrice(
                        medication.getPrice(),
                        "Brand medication",
                        sourceId
                ),
                currency
        );
    }

    private BigDecimal validatePrice(
            BigDecimal price,
            String itemName,
            Long sourceId
    ) {
        if (price == null) {
            throw new BadRequestAlertException(
                    itemName
                            + " default price is missing for source ID "
                            + sourceId
                            + ".",
                    ENTITY_NAME,
                    "defaultPrice.missing"
            );
        }

        if (price.signum() < 0) {
            throw new BadRequestAlertException(
                    itemName
                            + " default price is invalid for source ID "
                            + sourceId
                            + ".",
                    ENTITY_NAME,
                    "defaultPrice.invalid"
            );
        }

        return price;
    }

    private Currency validateCurrency(
            Currency itemCurrency,
            Currency requestedCurrency,
            String itemName,
            Long sourceId
    ) {
        if (itemCurrency == null) {
            throw new BadRequestAlertException(
                    itemName
                            + " currency is missing for source ID "
                            + sourceId
                            + ".",
                    ENTITY_NAME,
                    "currency.missing"
            );
        }

        if (requestedCurrency != null
                && itemCurrency != requestedCurrency) {
            throw new BadRequestAlertException(
                    itemName
                            + " currency does not match requested currency.",
                    ENTITY_NAME,
                    "currency.mismatch"
            );
        }

        return itemCurrency;
    }

    private Currency parseCurrency(
            Object value,
            String itemName,
            Long sourceId
    ) {
        if (value == null) {
            throw new BadRequestAlertException(
                    itemName
                            + " currency is missing for source ID "
                            + sourceId
                            + ".",
                    ENTITY_NAME,
                    "currency.missing"
            );
        }

        if (value instanceof Currency currency) {
            return currency;
        }

        try {
            return Currency.valueOf(
                    value.toString()
                            .trim()
                            .toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new BadRequestAlertException(
                    itemName
                            + " currency is invalid for source ID "
                            + sourceId
                            + ".",
                    ENTITY_NAME,
                    "currency.invalid"
            );
        }
    }

    private void validateActive(
            Boolean active,
            String itemName,
            Long sourceId
    ) {
        if (!Boolean.TRUE.equals(active)) {
            throw new BadRequestAlertException(
                    itemName
                            + " is inactive for source ID "
                            + sourceId
                            + ".",
                    ENTITY_NAME,
                    "item.inactive"
            );
        }
    }

    private BadRequestAlertException notFound(
            String itemName,
            Long sourceId
    ) {
        return new BadRequestAlertException(
                itemName
                        + " was not found for source ID "
                        + sourceId
                        + ".",
                ENTITY_NAME,
                "item.notfound"
        );
    }

    public record DefaultItemPrice(

            Long sourceId,

            String itemCode,

            String itemName,

            BigDecimal unitPrice,

            Currency currency

    ) {
    }
}