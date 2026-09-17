package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Discount;
import com.dazzle.asklepios.domain.Tax;
import com.dazzle.asklepios.domain.enumeration.DiscountType;
import com.dazzle.asklepios.domain.enumeration.TaxType;
import com.dazzle.asklepios.domain.enumeration.biling.PricingSource;
import com.dazzle.asklepios.service.DefaultItemPricingService.DefaultItemPrice;
import com.dazzle.asklepios.service.dto.BillingAdjustmentResolveRequest;
import com.dazzle.asklepios.service.dto.BillingAdjustmentResolveResponse;
import com.dazzle.asklepios.service.dto.BillingPricingResolutionDTO;
import com.dazzle.asklepios.service.dto.BillingPricingResolutionRequest;
import com.dazzle.asklepios.service.dto.BillingPricingResolveRequest;
import com.dazzle.asklepios.service.dto.BillingPricingResolveResponse;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillingPricingResolutionService {

    private static final Logger LOG =
            LoggerFactory.getLogger(
                    BillingPricingResolutionService.class
            );

    private static final String ENTITY_NAME =
            "billingPricingResolution";

    private final PriceListSetupService
            priceListSetupService;

    private final DefaultItemPricingService
            defaultItemPricingService;

    private final TaxService
            taxService;

    private final DiscountService
            discountService;

    public BillingPricingResolveResponse resolve(
            BillingPricingResolveRequest request
    ) {
        validateRequest(request);

        LocalDate pricingDate =
                request.pricingDate() == null
                        ? LocalDate.now()
                        : request.pricingDate();

        LOG.debug(
                "[RESOLVE] Billing pricing facilityId={} patientId={} encounterId={} payerId={} type={} sourceId={} currency={} date={}",
                request.facilityId(),
                request.patientId(),
                request.encounterId(),
                request.payerId(),
                request.billingItemType(),
                request.sourceId(),
                request.currency(),
                pricingDate
        );

        ResolvedPricing resolvedPricing =
                resolvePrice(
                        request,
                        pricingDate
                );

        Tax resolvedTax =
                taxService.resolveApplicableTax(
                        request.facilityId(),
                        request.currency(),
                        request.taxApplicableOn(),
                        pricingDate
                );

        Discount resolvedDiscount =
                resolveDiscount(
                        request,
                        resolvedPricing,
                        pricingDate
                );

        BillingPricingResolveResponse response =
                new BillingPricingResolveResponse(

                        resolvedPricing.priceListId(),
                        resolvedPricing.priceListItemId(),
                        resolvedPricing.priceListCode(),
                        resolvedPricing.priceListName(),
                        resolvedPricing.priceListItemCode(),
                        resolvedPricing.pricingVersion(),

                        resolvedPricing.itemCode(),
                        resolvedPricing.itemName(),

                        resolvedPricing.unitPrice(),
                        resolvedPricing.currency(),
                        resolvedPricing.pricingSource(),

                        resolvedTax == null
                                ? null
                                : resolvedTax.getId(),

                        resolvedTax == null
                                ? null
                                : resolvedTax.getTaxType(),

                        resolvedTax == null
                                ? null
                                : resolvedTax.getCalculationType(),

                        resolveTaxRate(
                                resolvedTax
                        ),

                        resolveTaxFixedAmount(
                                resolvedTax
                        ),

                        resolvedDiscount == null
                                ? null
                                : resolvedDiscount.getId(),

                        resolvedDiscount == null
                                ? null
                                : resolvedDiscount.getDiscountType(),

                        resolveDiscountRate(
                                resolvedDiscount
                        ),

                        resolveDiscountFixedAmount(
                                resolvedDiscount
                        ),

                        resolvedPricing.calculationOrder(),
                        resolvedPricing.roundingMode(),
                        resolvedPricing.roundingScale(),
                        resolvedPricing.requiresPreAuthorization()
                );

        LOG.info(
                "[RESOLVE] Billing pricing success pricingSource={} priceListId={} itemId={} unitPrice={} taxId={} discountId={}",
                response.pricingSource(),
                response.priceListId(),
                response.priceListItemId(),
                response.unitPrice(),
                response.taxId(),
                response.discountId()
        );

        return response;
    }

    public BillingAdjustmentResolveResponse resolveAdjustments(
            BillingAdjustmentResolveRequest request
    ) {
        validateAdjustmentRequest(request);

        LocalDate pricingDate =
                request.pricingDate() == null
                        ? LocalDate.now()
                        : request.pricingDate();

        Tax resolvedTax = null;
        if (request.taxApplicableOn() != null) {
            resolvedTax =
                    taxService.resolveApplicableTax(
                            request.facilityId(),
                            request.currency(),
                            request.taxApplicableOn(),
                            pricingDate
                    );
        }

        Discount resolvedDiscount = null;
        if (request.discountApplicableOn() != null) {
            resolvedDiscount =
                    discountService.resolveApplicableDiscount(
                            request.facilityId(),
                            request.discountApplicableOn(),
                            pricingDate
                    );
        }

        LOG.debug(
                "[RESOLVE_ADJUSTMENTS] facilityId={} taxApplicableOn={} discountApplicableOn={} taxId={} discountId={}",
                request.facilityId(),
                request.taxApplicableOn(),
                request.discountApplicableOn(),
                resolvedTax == null ? null : resolvedTax.getId(),
                resolvedDiscount == null
                        ? null
                        : resolvedDiscount.getId()
        );

        return new BillingAdjustmentResolveResponse(
                resolvedTax == null ? null : resolvedTax.getId(),
                resolvedTax == null ? null : resolvedTax.getCode(),
                resolvedTax == null ? null : resolvedTax.getName(),
                resolvedTax == null ? null : resolvedTax.getApplicableOn(),
                resolvedTax == null ? null : resolvedTax.getTaxType(),
                resolvedTax == null
                        ? null
                        : resolvedTax.getCalculationType(),
                resolveTaxRate(resolvedTax),
                resolveTaxFixedAmount(resolvedTax),
                resolvedDiscount == null ? null : resolvedDiscount.getId(),
                resolvedDiscount == null ? null : resolvedDiscount.getCode(),
                resolvedDiscount == null ? null : resolvedDiscount.getName(),
                resolvedDiscount == null ? null : resolvedDiscount.getApplicableOn(),
                resolvedDiscount == null
                        ? null
                        : resolvedDiscount.getDiscountType(),
                resolveDiscountRate(resolvedDiscount),
                resolveDiscountFixedAmount(resolvedDiscount)
        );
    }

    private void validateAdjustmentRequest(
            BillingAdjustmentResolveRequest request
    ) {
        if (request == null) {
            throw new BadRequestAlertException(
                    "Billing adjustment request is required.",
                    ENTITY_NAME,
                    "adjustment.request.required"
            );
        }

        if (request.facilityId() == null) {
            throw new BadRequestAlertException(
                    "Facility ID is required.",
                    ENTITY_NAME,
                    "facility.required"
            );
        }

        if (request.taxApplicableOn() == null
                && request.discountApplicableOn() == null) {
            throw new BadRequestAlertException(
                    "At least one applicable-on scope is required.",
                    ENTITY_NAME,
                    "applicableOn.required"
            );
        }
    }

    private ResolvedPricing resolvePrice(
            BillingPricingResolveRequest request,
            LocalDate pricingDate
    ) {

        BillingPricingResolutionDTO priceListPrice =
                priceListSetupService.resolve(
                        new BillingPricingResolutionRequest(
                                request.facilityId(),
                                request.patientId(),
                                request.encounterId(),
                                request.billingItemType(),
                                request.sourceId(),
                                request.patientInsuranceId(),
                                request.payerId(),
                                request.coverageType(),
                                request.currency(),
                                pricingDate,
                                request.visitType()
                        )
                );

        if (priceListPrice == null) {

            LOG.info(
                    "[RESOLVE] No price list found. Falling back to setup item price. "
                            + "coverageType={} itemType={} sourceId={}",
                    request.coverageType(),
                    request.billingItemType(),
                    request.sourceId()
            );

            return resolveFromDefaultItem(request);
        }

        validatePriceListPrice(
                request,
                priceListPrice
        );

        PricingSource pricingSource =
                Boolean.TRUE.equals(priceListPrice.cashFallback())
                        || "CASH".equals(priceListPrice.priceListType())
                        || "SELF_PAY".equals(priceListPrice.priceListType())
                        ? PricingSource.CASH_PRICE_LIST
                        : PricingSource.INSURANCE_PRICE_LIST;

        return new ResolvedPricing(
                priceListPrice.priceListSetupId(),
                priceListPrice.priceListSetupItemId(),
                null,
                priceListPrice.priceListName(),
                priceListPrice.itemCode(),
                null,
                priceListPrice.itemCode(),
                priceListPrice.itemName(),
                priceListPrice.unitPrice(),
                priceListPrice.currency(),
                pricingSource,
                defaultString(
                        priceListPrice.calculationOrder(),
                        "DISCOUNT_THEN_TAX"
                ),
                defaultString(
                        priceListPrice.roundingMode(),
                        "HALF_UP"
                ),
                priceListPrice.roundingScale() == null
                        ? 4
                        : priceListPrice.roundingScale(),
                priceListPrice.discountRate(),
                Boolean.TRUE.equals(
                        priceListPrice.requiresPreAuthorization()
                )
        );
    }

    private Discount resolveDiscount(
            BillingPricingResolveRequest request,
            ResolvedPricing resolvedPricing,
            LocalDate pricingDate
    ) {
        if (resolvedPricing.itemDiscountRate() != null
                && resolvedPricing.itemDiscountRate()
                .compareTo(BigDecimal.ZERO) > 0) {
            Discount itemDiscount =
                    new Discount();

            itemDiscount.setDiscountType(
                    DiscountType.PERCENTAGE
            );
            itemDiscount.setPercentage(
                    resolvedPricing.itemDiscountRate()
            );

            return itemDiscount;
        }

        return discountService.resolveApplicableDiscount(
                request.facilityId(),
                request.discountApplicableOn(),
                pricingDate
        );
    }

    private ResolvedPricing resolveFromDefaultItem(
            BillingPricingResolveRequest request
    ) {
        DefaultItemPrice defaultPrice =
                defaultItemPricingService.resolve(
                        request.billingItemType(),
                        request.sourceId(),
                        request.currency()
                );

        return new ResolvedPricing(
                null,
                null,
                null,
                null,
                null,
                null,
                defaultPrice.itemCode(),
                defaultPrice.itemName(),
                defaultPrice.unitPrice(),
                defaultPrice.currency(),
                PricingSource.DEFAULT_ITEM_PRICE,
                "DISCOUNT_THEN_TAX",
                "HALF_UP",
                4,
                null,
                false
        );
    }

    private void validatePriceListPrice(
            BillingPricingResolveRequest request,
            BillingPricingResolutionDTO price
    ) {
        if (price == null) {
            throw new EntityNotFoundException(
                    "No applicable price-list pricing was found."
            );
        }

        if (price.priceListSetupId() == null) {
            throw new EntityNotFoundException(
                    "Resolved price-list ID is missing."
            );
        }

        if (price.priceListSetupItemId() == null) {
            throw new EntityNotFoundException(
                    "Resolved price-list item ID is missing."
            );
        }

        if (price.unitPrice() == null
                || price.unitPrice().signum() < 0) {
            throw new BadRequestAlertException(
                    "Resolved price-list unit price is invalid.",
                    ENTITY_NAME,
                    "unitPrice.invalid"
            );
        }

        if (price.currency() == null) {
            throw new BadRequestAlertException(
                    "Resolved price-list currency is missing.",
                    ENTITY_NAME,
                    "currency.missing"
            );
        }

        if (price.currency() != request.currency()) {
            throw new BadRequestAlertException(
                    "Resolved price-list currency does not match request currency.",
                    ENTITY_NAME,
                    "currency.mismatch"
            );
        }
    }

    private BigDecimal resolveTaxRate(
            Tax tax
    ) {
        if (tax == null
                || tax.getTaxType()
                != TaxType.PERCENTAGE) {
            return BigDecimal.ZERO;
        }

        return defaultZero(
                tax.getPercentage()
        );
    }

    private BigDecimal resolveTaxFixedAmount(
            Tax tax
    ) {
        if (tax == null
                || tax.getTaxType()
                != TaxType.FIXED_AMOUNT) {
            return BigDecimal.ZERO;
        }

        return defaultZero(
                tax.getFixedAmount()
        );
    }

    private BigDecimal resolveDiscountRate(
            Discount discount
    ) {
        if (discount == null
                || discount.getDiscountType()
                != DiscountType.PERCENTAGE) {
            return BigDecimal.ZERO;
        }

        return defaultZero(
                discount.getPercentage()
        );
    }

    private BigDecimal resolveDiscountFixedAmount(
            Discount discount
    ) {
        if (discount == null
                || discount.getDiscountType()
                != DiscountType.FIXED_AMOUNT) {
            return BigDecimal.ZERO;
        }

        return defaultZero(
                discount.getFixedAmount()
        );
    }

    private void validateRequest(
            BillingPricingResolveRequest request
    ) {
        if (request == null) {
            throw new BadRequestAlertException(
                    "Billing pricing request is required.",
                    ENTITY_NAME,
                    "request.required"
            );
        }

        if (request.facilityId() == null) {
            throw new BadRequestAlertException(
                    "Facility ID is required.",
                    ENTITY_NAME,
                    "facility.required"
            );
        }

        if (request.patientId() == null) {
            throw new BadRequestAlertException(
                    "Patient ID is required.",
                    ENTITY_NAME,
                    "patient.required"
            );
        }

        if (request.encounterId() == null) {
            throw new BadRequestAlertException(
                    "Encounter ID is required.",
                    ENTITY_NAME,
                    "encounter.required"
            );
        }

        if (request.billingItemType() == null) {
            throw new BadRequestAlertException(
                    "Billing item type is required.",
                    ENTITY_NAME,
                    "billingItemType.required"
            );
        }

        if (request.sourceId() == null) {
            throw new BadRequestAlertException(
                    "Source ID is required.",
                    ENTITY_NAME,
                    "sourceId.required"
            );
        }

        if (request.currency() == null) {
            throw new BadRequestAlertException(
                    "Currency is required.",
                    ENTITY_NAME,
                    "currency.required"
            );
        }
    }

    private BigDecimal defaultZero(
            BigDecimal value
    ) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private String defaultString(
            String value,
            String defaultValue
    ) {
        return value == null
                || value.isBlank()
                ? defaultValue
                : value;
    }

    private record ResolvedPricing(

            Long priceListId,

            Long priceListItemId,

            String priceListCode,

            String priceListName,

            String priceListItemCode,

            Long pricingVersion,

            String itemCode,

            String itemName,

            BigDecimal unitPrice,

            com.dazzle.asklepios.domain.enumeration.Currency currency,

            PricingSource pricingSource,

            String calculationOrder,

            String roundingMode,

            Integer roundingScale,

            BigDecimal itemDiscountRate,

            Boolean requiresPreAuthorization

    ) {
    }
}