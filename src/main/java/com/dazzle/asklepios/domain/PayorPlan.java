package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.biling.PayorPlanType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

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

    // FK to payor (real FK in DB)
    @NotNull
    @Column(name = "payor_id", nullable = false)
    private Long payorId;

    @NotNull
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 50)
    private PayorPlanType planType;

    @Column(name = "network_id", length = 100)
    private String networkId;

    @Column(name = "coverage_type", length = 50)
    private String coverageType;

    @Column(name = "payer_nphies_id", length = 100)
    private String payerNphiesId;

    @Column(name = "waseel_plan_id", length = 100)
    private String waseelPlanId;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;


}
