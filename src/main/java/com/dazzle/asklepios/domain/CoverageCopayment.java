package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.EncounterType;
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
@Table(name = "coverage_copayment")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverageCopayment extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coverage_contract_id", nullable = false)
    private CoverageContract coverageContract;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "encounter_type", nullable = false, length = 30)
    private EncounterType encounterType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 30)
    private InsuranceCoverageType valueType;

    @NotNull
    @Column(name = "value_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal valueAmount;

    @Builder.Default
    @Column(name = "discount_on_excluded", nullable = false)
    private Boolean discountOnExcluded = false;

    @Builder.Default
    @Column(name = "discount_on_cash", nullable = false)
    private Boolean discountOnCash = false;

    @Builder.Default
    @Column(name = "discount_on_exceeded_cash", nullable = false)
    private Boolean discountOnExceededCash = false;

    @NotNull
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
