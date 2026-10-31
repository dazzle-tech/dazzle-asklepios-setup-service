package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.CoverageRuleTarget;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Items that a Coverage Limit rule does not apply to: a whole category,
 * a subcategory, or one item. Excluded items skip the limit and do not
 * consume it.
 */
@Entity
@Table(name = "coverage_term_exclusion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverageTermExclusion extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coverage_term_id", nullable = false)
    private CoverageTerm coverageTerm;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category_scope", nullable = false, length = 30)
    private CoverageRuleTarget categoryScope;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_item_type", nullable = false, length = 50)
    private BillingItemTypes billingItemType;

    @Column(name = "subcategory_key", length = 100)
    private String subcategoryKey;

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "item_name", length = 255)
    private String itemName;

    @NotNull
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
