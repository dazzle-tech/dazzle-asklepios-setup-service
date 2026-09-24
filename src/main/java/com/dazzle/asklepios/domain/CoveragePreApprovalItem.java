package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.CoverageRuleTarget;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
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

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coverage_pre_approval_item")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoveragePreApprovalItem extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pre_approval_id", nullable = false)
    private CoveragePreApproval preApproval;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 30)
    private CoverageRuleTarget itemType;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_category", length = 50)
    private ServiceCategory serviceCategory;

    @Column(name = "service_id")
    private Long serviceId;

    @Builder.Default
    @Column(name = "all_diagnoses", nullable = false)
    private Boolean allDiagnoses = false;

    @Column(name = "diagnosis_id")
    private Long diagnosisId;

    @Builder.Default
    @ElementCollection
    @CollectionTable(
            name = "coverage_pre_approval_item_diagnosis",
            joinColumns = @JoinColumn(name = "coverage_pre_approval_item_id", nullable = false)
    )
    @Column(name = "diagnosis_id", nullable = false)
    @OrderColumn(name = "sort_idx")
    private List<Long> diagnosisIds = new ArrayList<>();

    @NotNull
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
