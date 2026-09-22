package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.ApprovalCoverageCompany;
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

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "nphies_payers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NphiesPayer extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "nphies_id", nullable = false, unique = true, length = 100)
    private String nphiesId;

    @NotNull
    @Column(name = "name_en", nullable = false, length = 255)
    private String nameEn;

    @Column(name = "name_ar", length = 255)
    private String nameAr;

    @Column(name = "short_name", length = 100)
    private String shortName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @Column(name = "insurance_authority_license_no", length = 100)
    private String insuranceAuthorityLicenseNo;

    @Column(name = "commercial_registration_no", length = 100)
    private String commercialRegistrationNo;

    @Column(name = "vat_registration_no", length = 100)
    private String vatRegistrationNo;

    @Column(name = "unified_national_no", length = 100)
    private String unifiedNationalNo;

    @Column(name = "head_office_address", length = 2000)
    private String headOfficeAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private CountryDistrict city;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "contact_person", length = 255)
    private String contactPerson;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "mobile", length = 50)
    private String mobile;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "website", length = 255)
    private String website;

    @Builder.Default
    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_coverage_company", length = 20)
    private ApprovalCoverageCompany approvalCoverageCompany;

    @Builder.Default
    @ManyToMany(mappedBy = "insuranceCompanies")
    private Set<TpaDefinition> tpas = new HashSet<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "nphies_payer_child_companies",
            joinColumns = @JoinColumn(name = "parent_nphies_payer_id"),
            inverseJoinColumns = @JoinColumn(name = "child_nphies_payer_id")
    )
    private Set<NphiesPayer> childCompanies = new HashSet<>();

    @Builder.Default
    @ManyToMany(mappedBy = "childCompanies", fetch = FetchType.LAZY)
    private Set<NphiesPayer> parentCompanies = new HashSet<>();
}
