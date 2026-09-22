package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.CoverageRuleTarget;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.patient.YesNoQuestion;
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

@Entity
@Table(name = "coverage_exclusion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverageExclusion extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coverage_class_id")
    private CoverageClass coverageClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tpa_definition_id")
    private TpaDefinition tpaDefinition;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "exclusion_type", nullable = false, length = 30)
    private CoverageRuleTarget exclusionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_item_type", length = 50)
    private BillingItemTypes billingItemType;

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "item_name", length = 255)
    private String itemName;

    @Builder.Default
    @Column(name = "all_diagnoses", nullable = false)
    private Boolean allDiagnoses = false;

    @Column(name = "diagnosis_id")
    private Long diagnosisId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "encounter_type", nullable = false, length = 30)
    private EncounterType encounterType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "excluded_result", nullable = false, length = 30)
    private YesNoQuestion excludedResult;

    @NotNull
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
