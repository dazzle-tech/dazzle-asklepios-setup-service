package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.biling.ReadinessStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Table(name = "discharge_planning")
public class DischargePlanning extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // linkage (no FK)
    @NotNull
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @NotNull
    @Column(name = "encounter_id", nullable = false, unique = true)
    private Long encounterId;

    // Planned Discharge Readiness
    @NotNull
    @Column(name = "expected_discharge_date", nullable = false)
    private LocalDate expectedDischargeDate;

    @Column(name = "estimated_los", length = 50)
    private String estimatedLos;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "readiness_status", nullable = false, length = 50)
    private ReadinessStatus readinessStatus;

    // Clinical Clearance
    @NotNull
    @Column(name = "medical_condition_stable", nullable = false)
    private Boolean medicalConditionStable = false;

    @NotNull
    @Column(name = "vitals_stable", nullable = false)
    private Boolean vitalsStable = false;

    @NotNull
    @Column(name = "pending_investigations", nullable = false)
    private Boolean pendingInvestigations = false;

    // mobility/adl toggle
    @NotNull
    @Column(name = "mobility_adl_status", nullable = false)
    private Boolean mobilityAdlStatus = false;

    // Diagnosis ICD-10
    @NotNull
    @Column(name = "diagnosis_code", nullable = false, length = 20)
    private String diagnosisCode;

    @Column(name = "diagnosis_name", length = 255)
    private String diagnosisName;

    // Discharge Checklist
    @NotNull
    @Column(name = "final_med_reconciliation_completed", nullable = false)
    private Boolean finalMedReconciliationCompleted = false;

    @NotNull
    @Column(name = "discharge_summary_prepared", nullable = false)
    private Boolean dischargeSummaryPrepared = false;

    @NotNull
    @Column(name = "discharge_orders_signed", nullable = false)
    private Boolean dischargeOrdersSigned = false;

    @NotNull
    @Column(name = "nursing_discharge_report_done", nullable = false)
    private Boolean nursingDischargeReportDone = false;

    @NotNull
    @Column(name = "patient_family_informed", nullable = false)
    private Boolean patientFamilyInformed = false;

    @NotNull
    @Column(name = "transport_arranged", nullable = false)
    private Boolean transportArranged = false;

    // Post-Discharge Needs
    @Column(name = "medical_equipment", length = 1000)
    private String medicalEquipment;

    @NotNull
    @Column(name = "home_care_needed", nullable = false)
    private Boolean homeCareNeeded = false;

    @Column(name = "post_discharge_dietary_plan", length = 2000)
    private String postDischargeDietaryPlan;

    @Column(name = "post_discharge_social_needs", length = 2000)
    private String postDischargeSocialNeeds;

    // Patient Education
    @Column(name = "topics_covered", length = 2000)
    private String topicsCovered;

    @Column(name = "education_dietary_plan", length = 2000)
    private String educationDietaryPlan;

    @Column(name = "education_social_needs", length = 2000)
    private String educationSocialNeeds;

    @NotNull
    @Column(name = "material_leaflet", nullable = false)
    private Boolean materialLeaflet = false;

    @NotNull
    @Column(name = "material_verbal", nullable = false)
    private Boolean materialVerbal = false;

    @NotNull
    @Column(name = "material_video", nullable = false)
    private Boolean materialVideo = false;

    @NotNull
    @Column(name = "education_provided", nullable = false)
    private Boolean educationProvided = false;

    @NotNull
    @Column(name = "patient_understanding", nullable = false)
    private Boolean patientUnderstanding = false;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
