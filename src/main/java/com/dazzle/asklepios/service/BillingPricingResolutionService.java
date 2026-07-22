package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Discount;
import com.dazzle.asklepios.domain.Tax;
import com.dazzle.asklepios.domain.enumeration.DiscountType;
import com.dazzle.asklepios.domain.enumeration.TaxType;
import com.dazzle.asklepios.domain.enumeration.biling.PricingSource;
import com.dazzle.asklepios.service.dto.BillingPricingResolutionDTO;
import com.dazzle.asklepios.service.dto.BillingPricingResolutionRequest;
import com.dazzle.asklepios.service.dto.BillingPricingResolveRequest;
import com.dazzle.asklepios.service.dto.BillingPricingResolveResponse;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
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

    private final PriceListSetupService priceListSetupService;
    private final TaxService taxService;
    private final DiscountService discountService;

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

        /*
         * Use the method that already exists in PriceListSetupService.
         */
        BillingPricingResolutionDTO resolvedPrice =
                priceListSetupService.resolve(
                        new BillingPricingResolutionRequest(
                                request.facilityId(),
                                request.patientId(),
                                request.encounterId(),
                                request.billingItemType(),
                                request.sourceId(),
                                request.patientInsuranceId(),
                                request.payerId(),
                                request.currency(),
                                pricingDate
                        )
                );

        validateResolvedPrice(
                request,
                resolvedPrice
        );

        Tax resolvedTax =
                taxService.resolveApplicableTax(
                        request.facilityId(),
                        request.currency(),
                        request.taxApplicableOn(),
                        pricingDate
                );

        Discount resolvedDiscount =
                discountService
                        .resolveApplicableDiscount(
                                request.facilityId(),
                                request.discountApplicableOn(),
                                pricingDate
                        );

        BillingPricingResolveResponse response =
                new BillingPricingResolveResponse(

                        resolvedPrice.priceListSetupId(),
                        resolvedPrice.priceListSetupItemId(),

                        /*
                         * BillingPricingResolutionDTO currently does not
                         * contain a price-list code, so use null temporarily.
                         */
                        null,

                        resolvedPrice.priceListName(),

                        /*
                         * Use item code as the price-list item code.
                         */
                        resolvedPrice.itemCode(),

                        /*
                         * Add version to BillingPricingResolutionDTO later.
                         */
                        null,

                        resolvedPrice.itemCode(),
                        resolvedPrice.itemName(),

                        resolvedPrice.unitPrice(),
                        resolvedPrice.currency(),

                        request.payerId() == null
                                ? PricingSource.PRICE_LIST
                                : PricingSource.INSURANCE_PRICE_LIST,

                        resolvedTax == null
                                ? null
                                : resolvedTax.getId(),

                        resolvedTax == null
                                ? null
                                : resolvedTax.getTaxType(),

                        resolvedTax == null
                                ? null
                                : resolvedTax.getCalculationType(),

                        resolveTaxRate(resolvedTax),

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

                        resolvedPrice.calculationOrder() == null
                                ? "DISCOUNT_THEN_TAX"
                                : resolvedPrice.calculationOrder(),

                        resolvedPrice.roundingMode() == null
                                ? "HALF_UP"
                                : resolvedPrice.roundingMode(),

                        resolvedPrice.roundingScale() == null
                                ? 4
                                : resolvedPrice.roundingScale()
                );

        LOG.info(
                "[RESOLVE] Billing pricing success priceListId={} itemId={} unitPrice={} taxId={} discountId={}",
                response.priceListId(),
                response.priceListItemId(),
                response.unitPrice(),
                response.taxId(),
                response.discountId()
        );

        return response;
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

    private void validateResolvedPrice(
            BillingPricingResolveRequest request,
            BillingPricingResolutionDTO price
    ) {
        if (price == null) {
            throw new BadRequestAlertException(
                    "No applicable pricing configuration was found.",
                    ENTITY_NAME,
                    "price.notfound"
            );
        }

        if (price.priceListSetupId() == null) {
            throw new BadRequestAlertException(
                    "Resolved price-list ID is missing.",
                    ENTITY_NAME,
                    "priceListId.missing"
            );
        }

        if (price.priceListSetupItemId() == null) {
            throw new BadRequestAlertException(
                    "Resolved price-list item ID is missing.",
                    ENTITY_NAME,
                    "priceListItemId.missing"
            );
        }

        if (price.unitPrice() == null
                || price.unitPrice().signum() < 0) {
            throw new BadRequestAlertException(
                    "Resolved unit price is invalid.",
                    ENTITY_NAME,
                    "unitPrice.invalid"
            );
        }

        if (price.currency() == null) {
            throw new BadRequestAlertException(
                    "Resolved currency is missing.",
                    ENTITY_NAME,
                    "currency.missing"
            );
        }

        if (price.currency() != request.currency()) {
            throw new BadRequestAlertException(
                    "Resolved price currency does not match request currency.",
                    ENTITY_NAME,
                    "currency.mismatch"
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
}