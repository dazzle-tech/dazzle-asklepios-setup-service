package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Table(name = "payor_plan_item")
public class PayorPlanItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // many rules per plan
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private PayorPlan plan;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private BillingItemTypes itemType;

    @Column(name = "amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "coverage_type", nullable = false, length = 50)
    private InsuranceCoverageType coverageType;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}

