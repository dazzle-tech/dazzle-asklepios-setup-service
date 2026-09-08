package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.CoverageRuleTarget;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;
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

import java.math.BigDecimal;

@Entity
@Table(name = "coverage_term_item")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverageTermItem extends AbstractAuditingEntity<Long> {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "service_category", length = 50)
    private ServiceCategory serviceCategory;

    @Column(name = "service_id")
    private Long serviceId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 30)
    private InsuranceCoverageType valueType;

    @NotNull
    @Column(name = "limit_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal limitValue;

    @NotNull
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
