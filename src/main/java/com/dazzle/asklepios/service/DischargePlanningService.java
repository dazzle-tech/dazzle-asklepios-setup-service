package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DischargePlanning;
import com.dazzle.asklepios.repository.DischargePlanningRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.discharge.DischargePlanningSaveVM;
import com.dazzle.asklepios.web.rest.vm.discharge.DischargePlanningUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DischargePlanningService {

    private static final Logger LOG = LoggerFactory.getLogger(DischargePlanningService.class);

    private final DischargePlanningRepository repo;

    public DischargePlanningService(DischargePlanningRepository repo) {
        this.repo = repo;
    }

    /**
     * Upsert by encounter:
     * - if sheet exists for encounter -> update it
     * - else -> create new one
     */
    public DischargePlanning create(DischargePlanningSaveVM vm) {
        LOG.debug("Upsert DischargePlanning by encounter payload={}", vm);

        validateMandatory(vm.expectedDischargeDate(), vm.readinessStatus(), vm.diagnosisCode());

        return repo.findByEncounterId(vm.encounterId())
                .map(existing -> {
                    mapSaveVmToEntity(existing, vm);
                    return repo.save(existing);
                })
                .orElseGet(() -> {
                    DischargePlanning d = DischargePlanning.builder().build();
                    mapSaveVmToEntity(d, vm);
                    return repo.save(d);
                });
    }

    // keep normal update if ever needed explicitly
    public DischargePlanning update(DischargePlanningUpdateVM vm) {
        LOG.debug("Update DischargePlanning payload={}", vm);

        DischargePlanning existing = repo.findById(vm.id())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notFound", "dischargePlanning", "Discharge Planning sheet not found."
                ));

        validateMandatory(vm.expectedDischargeDate(), vm.readinessStatus(), vm.diagnosisCode());

        existing.setPatientId(vm.patientId());
        existing.setEncounterId(vm.encounterId());

        existing.setExpectedDischargeDate(vm.expectedDischargeDate());
        existing.setEstimatedLos(vm.estimatedLos());
        existing.setReadinessStatus(vm.readinessStatus());

        existing.setMedicalConditionStable(bool(vm.medicalConditionStable()));
        existing.setVitalsStable(bool(vm.vitalsStable()));
        existing.setPendingInvestigations(bool(vm.pendingInvestigations()));
        existing.setMobilityAdlStatus(bool(vm.mobilityAdlStatus()));

        existing.setDiagnosisCode(vm.diagnosisCode());
        existing.setDiagnosisName(vm.diagnosisName());

        existing.setFinalMedReconciliationCompleted(bool(vm.finalMedReconciliationCompleted()));
        existing.setDischargeSummaryPrepared(bool(vm.dischargeSummaryPrepared()));
        existing.setDischargeOrdersSigned(bool(vm.dischargeOrdersSigned()));
        existing.setNursingDischargeReportDone(bool(vm.nursingDischargeReportDone()));
        existing.setPatientFamilyInformed(bool(vm.patientFamilyInformed()));
        existing.setTransportArranged(bool(vm.transportArranged()));

        existing.setMedicalEquipment(vm.medicalEquipment());
        existing.setHomeCareNeeded(bool(vm.homeCareNeeded()));
        existing.setPostDischargeDietaryPlan(vm.postDischargeDietaryPlan());
        existing.setPostDischargeSocialNeeds(vm.postDischargeSocialNeeds());

        existing.setTopicsCovered(vm.topicsCovered());
        existing.setEducationDietaryPlan(vm.educationDietaryPlan());
        existing.setEducationSocialNeeds(vm.educationSocialNeeds());

        existing.setMaterialLeaflet(bool(vm.materialLeaflet()));
        existing.setMaterialVerbal(bool(vm.materialVerbal()));
        existing.setMaterialVideo(bool(vm.materialVideo()));
        existing.setEducationProvided(bool(vm.educationProvided()));
        existing.setPatientUnderstanding(bool(vm.patientUnderstanding()));

        existing.setIsActive(vm.isActive() != null ? vm.isActive() : existing.getIsActive());

        return repo.save(existing);
    }

    private void mapSaveVmToEntity(DischargePlanning d, DischargePlanningSaveVM vm) {
        d.setPatientId(vm.patientId());
        d.setEncounterId(vm.encounterId());

        d.setExpectedDischargeDate(vm.expectedDischargeDate());
        d.setEstimatedLos(vm.estimatedLos());
        d.setReadinessStatus(vm.readinessStatus());

        d.setMedicalConditionStable(bool(vm.medicalConditionStable()));
        d.setVitalsStable(bool(vm.vitalsStable()));
        d.setPendingInvestigations(bool(vm.pendingInvestigations()));
        d.setMobilityAdlStatus(bool(vm.mobilityAdlStatus()));

        d.setDiagnosisCode(vm.diagnosisCode());
        d.setDiagnosisName(vm.diagnosisName());

        d.setFinalMedReconciliationCompleted(bool(vm.finalMedReconciliationCompleted()));
        d.setDischargeSummaryPrepared(bool(vm.dischargeSummaryPrepared()));
        d.setDischargeOrdersSigned(bool(vm.dischargeOrdersSigned()));
        d.setNursingDischargeReportDone(bool(vm.nursingDischargeReportDone()));
        d.setPatientFamilyInformed(bool(vm.patientFamilyInformed()));
        d.setTransportArranged(bool(vm.transportArranged()));

        d.setMedicalEquipment(vm.medicalEquipment());
        d.setHomeCareNeeded(bool(vm.homeCareNeeded()));
        d.setPostDischargeDietaryPlan(vm.postDischargeDietaryPlan());
        d.setPostDischargeSocialNeeds(vm.postDischargeSocialNeeds());

        d.setTopicsCovered(vm.topicsCovered());
        d.setEducationDietaryPlan(vm.educationDietaryPlan());
        d.setEducationSocialNeeds(vm.educationSocialNeeds());

        d.setMaterialLeaflet(bool(vm.materialLeaflet()));
        d.setMaterialVerbal(bool(vm.materialVerbal()));
        d.setMaterialVideo(bool(vm.materialVideo()));
        d.setEducationProvided(bool(vm.educationProvided()));
        d.setPatientUnderstanding(bool(vm.patientUnderstanding()));

        d.setIsActive(vm.isActive() != null ? vm.isActive() : true);
    }

    private void validateMandatory(java.time.LocalDate expectedDate,
                                   Object readinessStatus,
                                   String diagnosisCode) {
        if (expectedDate == null) {
            throw new BadRequestAlertException(
                    "expectedDateMandatory", "dischargePlanning", "Expected discharge date is mandatory."
            );
        }
        if (readinessStatus == null) {
            throw new BadRequestAlertException(
                    "readinessMandatory", "dischargePlanning", "Readiness status is mandatory."
            );
        }
        if (diagnosisCode == null || diagnosisCode.isBlank()) {
            throw new BadRequestAlertException(
                    "diagnosisMandatory", "dischargePlanning", "Diagnosis is mandatory."
            );
        }
    }

    private boolean bool(Boolean v) {
        return v != null && v;
    }

    // ------------ READ ------------
    @Transactional(readOnly = true)
    public java.util.Optional<DischargePlanning> getObjectByEncounterId(Long encounterId) {
        return repo.findByEncounterId(encounterId);
    }

    @Transactional(readOnly = true)
    public Page<DischargePlanning> getByPatient(Long patientId, Pageable pageable) {
        return repo.findByPatientId(patientId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<DischargePlanning> getByPatientAndEncounter(Long patientId, Long encounterId, Pageable pageable) {
        return repo.findByPatientIdAndEncounterId(patientId, encounterId, pageable);
    }
}
