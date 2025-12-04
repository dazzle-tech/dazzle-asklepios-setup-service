package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;
import com.dazzle.asklepios.domain.enumeration.biling.PayorPlanType;
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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Table(name = "payor_plan")
public class PayorPlan extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Payor can not be empty")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payor_id", nullable = false)
    private Payor payor;

    // Information
    @NotNull(message = "Name can not be empty")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull(message = "Plan Type can not be empty")
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 50)
    private PayorPlanType planType;

    // Coverage rule (single)
    @NotNull(message = "Item Type can not be empty")
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private BillingItemTypes itemType;

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Coverage can not be empty")
    @Enumerated(EnumType.STRING)
    @Column(name = "coverage_type", nullable = false, length = 50)
    private InsuranceCoverageType coverageType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
