package com.dazzle.asklepios.web.rest.vm.discharge;

import com.dazzle.asklepios.domain.enumeration.biling.ReadinessStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;

public record DischargePlanningUpdateVM(

        @NotNull Long id,

        @NotNull Long patientId,
        @NotNull Long encounterId,

        @NotNull LocalDate expectedDischargeDate,
        String estimatedLos,
        @NotNull ReadinessStatus readinessStatus,

        Boolean medicalConditionStable,
        Boolean vitalsStable,
        Boolean pendingInvestigations,
        Boolean mobilityAdlStatus,  // toggle

        @NotBlank @Size(max = 20)
        String diagnosisCode,
        @Size(max = 255)
        String diagnosisName,

        Boolean finalMedReconciliationCompleted,
        Boolean dischargeSummaryPrepared,
        Boolean dischargeOrdersSigned,
        Boolean nursingDischargeReportDone,
        Boolean patientFamilyInformed,
        Boolean transportArranged,

        String medicalEquipment,
        Boolean homeCareNeeded,
        String postDischargeDietaryPlan,
        String postDischargeSocialNeeds,

        String topicsCovered,
        String educationDietaryPlan,
        String educationSocialNeeds,

        Boolean materialLeaflet,
        Boolean materialVerbal,
        Boolean materialVideo,
        Boolean educationProvided,
        Boolean patientUnderstanding,

        Boolean isActive

) implements Serializable {}
