package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.BillingRule;
import com.dazzle.asklepios.domain.enumeration.TestType;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.repository.BillingRuleRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BillingRuleReferenceService {

    private static final String ENTITY_NAME = "billingRule";

    private final BillingRuleRepository billingRuleRepository;

    public BillingRuleReferenceService(
            BillingRuleRepository billingRuleRepository
    ) {
        this.billingRuleRepository = billingRuleRepository;
    }

    public BillingRule resolveOptional(
            Long billingRuleId,
            BillingItemTypes expectedItemType
    ) {
        if (billingRuleId == null) {
            return null;
        }

        BillingRule billingRule =
                billingRuleRepository
                        .findById(billingRuleId)
                        .orElseThrow(
                                () ->
                                        new NotFoundAlertException(
                                                "Billing rule not found with id "
                                                        + billingRuleId,
                                                ENTITY_NAME,
                                                "notfound"
                                        )
                        );

        if (
                expectedItemType != null
                        && billingRule.getBillingItemType()
                                != expectedItemType
        ) {
            throw new BadRequestAlertException(
                    "Billing rule item type must be "
                            + expectedItemType,
                    ENTITY_NAME,
                    "item.type.mismatch"
            );
        }

        return billingRule;
    }

    public BillingItemTypes toBillingItemType(
            TestType testType
    ) {
        if (testType == null) {
            return null;
        }

        return switch (testType) {
            case LABORATORY, MICROBIOLOGY ->
                    BillingItemTypes.LABORATORY;
            case RADIOLOGY ->
                    BillingItemTypes.RADIOLOGY;
            case PATHOLOGY ->
                    BillingItemTypes.PATHOLOGY;
        };
    }
}
