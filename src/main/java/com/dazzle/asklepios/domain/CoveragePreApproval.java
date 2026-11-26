package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.CoverageApprovalScope;
import com.dazzle.asklepios.domain.enumeration.CoveragePreApprovalLevel;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
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
@Table(name = "coverage_pre_approval")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoveragePreApproval extends AbstractAuditingEntity<Long> {

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
    @Column(name = "approval_scope", nullable = false, length = 30)
    private CoverageApprovalScope approvalScope;

    @Column(name = "facility_id")
    private Long facilityId;

    @Column(name = "department_id")
    private Long departmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "encounter_type", length = 30)
    private EncounterType encounterType;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", length = 30)
    private InsuranceCoverageType valueType;

    @Column(name = "limit_value", precision = 19, scale = 4)
    private BigDecimal limitValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_level", length = 30)
    private CoveragePreApprovalLevel targetLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 50)
    private BillingItemTypes source;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "subcategory_key", length = 100)
    private String subcategoryKey;

    @NotNull
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
