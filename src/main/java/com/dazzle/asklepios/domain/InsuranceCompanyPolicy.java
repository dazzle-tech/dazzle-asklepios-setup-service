package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDate;

@Entity
@Table(name = "insurance_company_policy")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceCompanyPolicy extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nphies_payer_id", nullable = false)
    private NphiesPayer insuranceCompany;

    @NotNull
    @Column(name = "policy_name", nullable = false, length = 255)
    private String policyName;

    @NotNull
    @Column(name = "policy_number", nullable = false, length = 100)
    private String policyNumber;

    @NotNull
    @Column(name = "policy_code", nullable = false, length = 100)
    private String policyCode;

    @NotNull
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @NotNull
    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;
}
