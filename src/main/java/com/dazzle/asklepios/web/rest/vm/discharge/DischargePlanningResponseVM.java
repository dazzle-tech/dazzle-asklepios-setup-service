package com.dazzle.asklepios.web.rest.vm.discharge;

import com.dazzle.asklepios.domain.DischargePlanning;
import com.dazzle.asklepios.domain.enumeration.biling.ReadinessStatus;

import java.time.Instant;
import java.time.LocalDate;

public record DischargePlanningResponseVM(
        Long id,
        Long patientId,
        Long encounterId,

        LocalDate expectedDischargeDate,
        String estimatedLos,
        ReadinessStatus readinessStatus,

        Boolean medicalConditionStable,
        Boolean vitalsStable,
        Boolean pendingInvestigations,
        Boolean mobilityAdlStatus,

        String diagnosisCode,
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

        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {
    public static DischargePlanningResponseVM ofEntity(DischargePlanning d) {
        return new DischargePlanningResponseVM(
                d.getId(),
                d.getPatientId(),
                d.getEncounterId(),
                d.getExpectedDischargeDate(),
                d.getEstimatedLos(),
                d.getReadinessStatus(),
                d.getMedicalConditionStable(),
                d.getVitalsStable(),
                d.getPendingInvestigations(),
                d.getMobilityAdlStatus(),
                d.getDiagnosisCode(),
                d.getDiagnosisName(),
                d.getFinalMedReconciliationCompleted(),
                d.getDischargeSummaryPrepared(),
                d.getDischargeOrdersSigned(),
                d.getNursingDischargeReportDone(),
                d.getPatientFamilyInformed(),
                d.getTransportArranged(),
                d.getMedicalEquipment(),
                d.getHomeCareNeeded(),
                d.getPostDischargeDietaryPlan(),
                d.getPostDischargeSocialNeeds(),
                d.getTopicsCovered(),
                d.getEducationDietaryPlan(),
                d.getEducationSocialNeeds(),
                d.getMaterialLeaflet(),
                d.getMaterialVerbal(),
                d.getMaterialVideo(),
                d.getEducationProvided(),
                d.getPatientUnderstanding(),
                d.getIsActive(),
                d.getCreatedDate(),
                d.getLastModifiedDate()
        );
    }
}
