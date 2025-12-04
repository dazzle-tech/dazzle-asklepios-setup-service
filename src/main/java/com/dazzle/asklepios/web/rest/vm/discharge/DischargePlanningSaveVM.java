package com.dazzle.asklepios.web.rest.vm.discharge;

import com.dazzle.asklepios.domain.enumeration.biling.ReadinessStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;

public record DischargePlanningSaveVM(

        @NotNull Long patientId,
        @NotNull Long encounterId,

        // Planned Discharge Readiness (mandatory)
        @NotNull LocalDate expectedDischargeDate,
        String estimatedLos,
        @NotNull ReadinessStatus readinessStatus,

        // Clinical Clearance
        Boolean medicalConditionStable,
        Boolean vitalsStable,
        Boolean pendingInvestigations,
        Boolean mobilityAdlStatus,   // toggle

        // Diagnosis (mandatory)
        @NotBlank @Size(max = 20)
        String diagnosisCode,
        @Size(max = 255)
        String diagnosisName,

        // Checklist
        Boolean finalMedReconciliationCompleted,
        Boolean dischargeSummaryPrepared,
        Boolean dischargeOrdersSigned,
        Boolean nursingDischargeReportDone,
        Boolean patientFamilyInformed,
        Boolean transportArranged,

        // Post-Discharge Needs
        @Size(max = 1000)
        String medicalEquipment,
        Boolean homeCareNeeded,
        @Size(max = 2000)
        String postDischargeDietaryPlan,
        @Size(max = 2000)
        String postDischargeSocialNeeds,

        // Patient Education
        @Size(max = 2000)
        String topicsCovered,
        @Size(max = 2000)
        String educationDietaryPlan,
        @Size(max = 2000)
        String educationSocialNeeds,

        Boolean materialLeaflet,
        Boolean materialVerbal,
        Boolean materialVideo,
        Boolean educationProvided,
        Boolean patientUnderstanding,

        Boolean isActive

) implements Serializable {
}
