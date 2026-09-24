package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.CoverageBasis;
import com.dazzle.asklepios.domain.enumeration.CoverageDiagnosisScope;
import com.dazzle.asklepios.domain.enumeration.CoveragePeriodBasis;
import com.dazzle.asklepios.domain.enumeration.CoverageTermType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coverage_term")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverageTerm extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coverage_class_id", nullable = false)
    private CoverageClass coverageClass;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "term_type", nullable = false, length = 30)
    private CoverageTermType termType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "diagnosis_scope", nullable = false, length = 30)
    private CoverageDiagnosisScope diagnosisScope;

    @Column(name = "diagnosis_id")
    private Long diagnosisId;

    @Builder.Default
    @ElementCollection
    @CollectionTable(
            name = "coverage_term_diagnosis",
            joinColumns = @JoinColumn(name = "coverage_term_id", nullable = false)
    )
    @Column(name = "diagnosis_id", nullable = false)
    @OrderColumn(name = "sort_idx")
    private List<Long> diagnosisIds = new ArrayList<>();

    @NotNull
    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @NotNull
    @Builder.Default
    @Column(name = "all_departments", nullable = false)
    private Boolean allDepartments = false;

    @Column(name = "department_id")
    private Long departmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "encounter_type", length = 30)
    private EncounterType encounterType;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_basis", length = 30)
    private CoveragePeriodBasis periodBasis;

    @Enumerated(EnumType.STRING)
    @Column(name = "coverage_basis", length = 20)
    private CoverageBasis coverageBasis;

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
