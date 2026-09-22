package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.ApprovalCoverageCompany;
import com.dazzle.asklepios.domain.enumeration.CoverageBasis;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coverage_contract")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverageContract extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "guarantor_type", nullable = false, length = 50)
    private GuarantorType guarantorType;

    @NotNull
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @NotBlank
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @NotBlank
    @Column(name = "policy_number", nullable = false, length = 100)
    private String policyNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "coverage_basis", nullable = false, length = 20)
    private CoverageBasis coverageBasis;

    @NotNull
    @Column(name = "insurance_payer_id", nullable = false)
    private Long insurancePayerId;

    @NotNull
    @Column(name = "price_list_setup_id", nullable = false)
    private Long priceListSetupId;

    @Column(name = "parent_payer_id")
    private Long parentPayerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_coverage_company", length = 20)
    private ApprovalCoverageCompany approvalCoverageCompany;

    @NotNull
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
