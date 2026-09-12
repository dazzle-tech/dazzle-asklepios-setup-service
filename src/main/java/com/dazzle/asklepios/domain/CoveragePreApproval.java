package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.CoverageApprovalScope;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
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
    @JoinColumn(name = "coverage_contract_id")
    private CoverageContract coverageContract;

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

    @NotNull
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
