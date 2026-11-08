package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.BillingRule;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.biling.BillingRulePaymentType;
import com.dazzle.asklepios.domain.enumeration.biling.BillingTrigger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface BillingRuleRepository extends JpaRepository<BillingRule, Long> {

    Page<BillingRule> findAllByOrderByNameAsc(Pageable pageable);

    Page<BillingRule> findByBillingItemType(BillingItemTypes billingItemType, Pageable pageable);

    Page<BillingRule> findByPaymentType(BillingRulePaymentType paymentType, Pageable pageable);

    Page<BillingRule> findByBillingTrigger(BillingTrigger billingTrigger, Pageable pageable);

    Page<BillingRule> findByIsDefault(Boolean isDefault, Pageable pageable);

    Page<BillingRule> findByCreatedDateGreaterThanEqualAndCreatedDateLessThan(
            Instant from,
            Instant to,
            Pageable pageable
    );

    Page<BillingRule> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<BillingRule> findByNameContainingIgnoreCaseAndBillingItemType(
            String name,
            BillingItemTypes billingItemType,
            Pageable pageable
    );

    Optional<BillingRule> findFirstByBillingItemTypeAndIsDefaultTrue(BillingItemTypes billingItemType);

    Optional<BillingRule> findFirstByIsDefaultTrue();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}