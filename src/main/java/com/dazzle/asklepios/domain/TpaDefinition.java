package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.ApprovalCoverageCompany;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tpa_definitions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TpaDefinition extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "tpa_code", nullable = false, unique = true, length = 100)
    private String tpaCode;

    @NotNull
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Builder.Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "guarantor_type", nullable = false, length = 50)
    private GuarantorType guarantorType = GuarantorType.TPA;

    @NotNull
    @Column(name = "activation_date", nullable = false)
    private LocalDate activationDate;

    @Builder.Default
    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_coverage_company", length = 20)
    private ApprovalCoverageCompany approvalCoverageCompany;

    @Column(name = "tax_registration_no", length = 100)
    private String taxRegistrationNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private CountryDistrict city;

    @Column(name = "address", length = 2000)
    private String address;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tpa_payor_companies",
            joinColumns = @JoinColumn(name = "tpa_id"),
            inverseJoinColumns = @JoinColumn(name = "nphies_payer_id")
    )
    private Set<NphiesPayer> insuranceCompanies = new HashSet<>();
}
