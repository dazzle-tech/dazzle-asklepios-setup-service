package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.ReferralPriority;
import com.dazzle.asklepios.domain.enumeration.ReferralType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Table(name = "referral_request")
public class ReferralRequest extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // linkage (no FK)
    @NotNull
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "encounter_id")
    private Long encounterId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "referral_type", nullable = false, length = 20)
    private ReferralType referralType = ReferralType.INTERNAL;

    // used only if EXTERNAL
    @Column(name = "facility_id")
    private Long facilityId;

    @NotNull
    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @NotNull
    @Column(name = "referral_reason", nullable = false, length = 1000)
    private String referralReason;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 30)
    private ReferralPriority priority;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
