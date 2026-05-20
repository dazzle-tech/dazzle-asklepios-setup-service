package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.CoverageClassType;
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

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Table(name = "payor_plan_coverage_class")
public class PayorPlanCoverageClass extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private PayorPlan plan;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "coverage_class_type", nullable = false, length = 50)
    private CoverageClassType coverageClassType;

    @NotNull
    @Column(name = "coverage_class_value", nullable = false, length = 100)
    private String coverageClassValue;

    @Column(name = "coverage_class_name", length = 255)
    private String coverageClassName;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}