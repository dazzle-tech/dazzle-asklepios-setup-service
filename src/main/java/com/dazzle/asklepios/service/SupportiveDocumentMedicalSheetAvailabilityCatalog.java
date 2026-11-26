package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.enumeration.MedicalSheets;
import com.dazzle.asklepios.web.rest.vm.supportivedocument.SupportiveDocumentMedicalSheetAvailabilityVM;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Exhaustive availability of all {@link MedicalSheets} values for Setup UI display.
 * Supported sheets expose selectable requirement codes via {@link SupportiveDocumentRequirementCatalog}.
 */
@Component
public class SupportiveDocumentMedicalSheetAvailabilityCatalog {

    private static final Set<MedicalSheets> SUPPORTED = EnumSet.of(
            MedicalSheets.CLINICAL_VISIT,
            MedicalSheets.DIAGNOSTICS_ORDER,
            MedicalSheets.PRESCRIPTION,
            MedicalSheets.CONSULTATION,
            MedicalSheets.PROCEDURES,
            MedicalSheets.PATIENT_HISTORY,
            MedicalSheets.ALLERGIES,
            MedicalSheets.MEDICAL_WARNINGS,
            MedicalSheets.MEDICATIONS_RECORD,
            MedicalSheets.VACCINE_RECCORD,
            MedicalSheets.DIAGNOSTICS_RESULT,
            MedicalSheets.DENTAL_CARE,
            MedicalSheets.OBSERVATION,
            MedicalSheets.VACCINATION,
            MedicalSheets.REFERRAL_REQUEST,
            MedicalSheets.GLASGOW_COMA_SCALE,
            MedicalSheets.PROGRESS_NOTES,
            MedicalSheets.FLACC_NEONATES_PAIN_ASSESSMENT,
            MedicalSheets.SERVICE_AND_PRODUCTS,
            MedicalSheets.CHIEF_COMPLAIN,
            MedicalSheets.PAIN_ASSESSMENT,
            MedicalSheets.GENERAL_ASSESSMENT,
            MedicalSheets.SERVICEANDPRODUCTS,
            MedicalSheets.FORMS,
            MedicalSheets.NURSE_ASSESSMENT,
            MedicalSheets.UCC_MEDICATION_ORDER,
            MedicalSheets.PHYSICIAN_ASSESSMENT,
            MedicalSheets.DENTAL_PROCEDURES,
            MedicalSheets.NEPHROLOGY_ASSESSMENT,
            MedicalSheets.DIALYSIS_SESSION
    );

    public List<SupportiveDocumentMedicalSheetAvailabilityVM> listAll() {
        MedicalSheets[] values = MedicalSheets.values();
        return Arrays.stream(values)
                .map(sheet -> toVm(sheet, indexOf(sheet, values)))
                .toList();
    }

    private static int indexOf(MedicalSheets sheet, MedicalSheets[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == sheet) {
                return i + 1;
            }
        }
        return 0;
    }

    private static SupportiveDocumentMedicalSheetAvailabilityVM toVm(MedicalSheets sheet, int order) {
        SheetInfo info = info(sheet);
        boolean supported = SUPPORTED.contains(sheet);
        return new SupportiveDocumentMedicalSheetAvailabilityVM(
                sheet.name(),
                info.name(),
                info.path(),
                supported,
                supported ? null : info.unsupportedReason(),
                order
        );
    }

    private static SheetInfo info(MedicalSheets sheet) {
        return switch (sheet) {
            case CLINICAL_VISIT -> ok("Clinical Visit", "/clinical-visit");
            case DIAGNOSTICS_ORDER -> ok("Diagnostics Order", "/diagnostics-order");
            case PRESCRIPTION -> ok("Prescription", "/prescription");
            case DRUG_ORDER -> blocked(
                    "Medication Order",
                    "/medication-order",
                    "No patient-service drug-order entity; UCC medication order is separate"
            );
            case CONSULTATION -> ok("Consultation", "/consultation");
            case PROCEDURES -> ok("Procedures", "/procedures");
            case PATIENT_HISTORY -> ok("Patient History", "/patient-history");
            case ALLERGIES -> ok("Allergies", "/allergies");
            case MEDICAL_WARNINGS -> ok("Medical Warnings", "/medical-warnings");
            case MEDICATIONS_RECORD -> ok("Medications Record", "/medications-record");
            case PSYCHOLOGICAL_EXAM -> blocked(
                    "Psychological Exam",
                    "/psychological-exam",
                    "Working GS save: /encounter/save-psychological-exam (encounter_key). "
                            + "No Long encounter-id bridge or GS Feign client for validators"
            );
            case AUDIOMETRY_PURETONE -> blocked(
                    "Audiometry Puretone",
                    "/audiometry",
                    "Working GS save/load: /encounter/save-audiometry-puretone and list "
                            + "(encounter_key). No Long encounter-id bridge or GS Feign client"
            );
            case OPTOMETRIC_EXAM -> blocked(
                    "Optometric Exam",
                    "/optometric-exam",
                    "Working GS save/load: /encounter/save-optometric-exam and list "
                            + "(encounter_key). No Long encounter-id bridge or GS Feign client"
            );
            case VACCINE_RECCORD -> ok("Vaccine Record", "/vaccine-record");
            case DIAGNOSTICS_RESULT -> ok("Diagnostics Test Result", "/diagnostics-result");
            case DENTAL_CARE -> ok("Dental Care", "/dental-care");
            case CARDIOLOGY -> blocked("Cardiology", "/cardiology", "No working backend implementation");
            case OBSERVATION -> ok("Observation", "/observations");
            case VACCINATION -> ok("Vaccination", "/vaccination");
            case OPERATION_REQUESTS -> blocked(
                    "Operation Requests",
                    "/operation-request",
                    "No working patient-service implementation for supportive-doc validation"
            );
            case DOCTOR_ROUND -> blocked(
                    "Doctor Round",
                    "/doctor-round",
                    "Working GS doctor-round APIs (encounter_key). "
                            + "No Long encounter-id bridge or GS Feign client for validators"
            );
            case DAY_CASE -> blocked("Day Case", "/day-case", "No clinical documentation entity for completion");
            case BEDSIDE_PROCEDURES_REQUEST -> blocked(
                    "Bedside Procedures",
                    "/bedside-procedures-requests",
                    "No working backend implementation"
            );
            case REFERRAL_REQUEST -> ok("Referral Request", "/referral-request");
            case BLOOD_ORDER -> blocked("Blood Order", "/blood-order", "No working backend implementation");
            case IV_FLUID_ORDER -> blocked("IV Fluid Order", "/iv-fluid-order", "No working backend implementation");
            case INTAKE_OUTPUT_BALANCE -> blocked(
                    "Intake Output Balance",
                    "/intake-output-balance",
                    "No working backend implementation"
            );
            case RISK_ASSESSMENTS -> blocked(
                    "Risk Assessments",
                    "",
                    "Umbrella enum only; no dedicated entity (use GCS/FLACC sheets)"
            );
            case MULTIDISCIPLINARY_TEAM_NOTES -> blocked(
                    "Multidisciplinary Team Notes",
                    "/multidisciplinary-team-notes",
                    "No working backend implementation"
            );
            case NUTRITION_STATE_ASSESSMENT -> blocked(
                    "Nutrition State Assessment",
                    "/nutrition-state-asssessment",
                    "No working backend implementation"
            );
            case PHYSICIAN_ORDER_SUMMARY -> blocked(
                    "Physician Order Summary",
                    "/physician-order-summary",
                    "UI aggregate only; no documentation entity"
            );
            case CARE_PLAN_AND_GOALS -> blocked(
                    "Care Plan & Goals",
                    "/care-plan-and-goals",
                    "No working backend implementation"
            );
            case DISCHARGE_PLANNING -> blocked(
                    "Discharge Planning",
                    "/discharge-planning",
                    "Setup-service discharge config; not patient clinical documentation"
            );
            case PREGNANCY_FOLLOW_UP -> blocked(
                    "Pregnancy Follow-up",
                    "/pregnancy-follow-up",
                    "No working backend implementation"
            );
            case MORSE_FALL_SCALE -> blocked(
                    "Morse Fall Scale",
                    "/morse-fall-scale",
                    "No working backend implementation"
            );
            case HENDRICH_FALL_RISK -> blocked(
                    "Hendrich Fall Risk",
                    "/hendrich-fall-risk",
                    "No working backend implementation"
            );
            case STRATIFY_SCALE -> blocked("STRATIFY Scale", "/stratify-scale", "No working backend implementation");
            case JOHNS_HOPKINS_FALL_RISK_ASSESSMENT_TOOL -> blocked(
                    "Johns Hopkins Fall Risk Assessment Tool",
                    "/johns-hopkins-tool",
                    "No working backend implementation"
            );
            case BRADEN_SCALE_FOR_PRESSURE_ULCER -> blocked(
                    "Braden Scale for Pressure Ulcer",
                    "/pressure-ulce-risk-assessment",
                    "No working backend implementation"
            );
            case GLASGOW_COMA_SCALE -> ok("Glasgow Coma Scale", "/glasgow-coma-scale");
            case VTE_RISK_ASSESSMENT -> blocked(
                    "VTE Risk Assessment",
                    "/vte-risk-assessment",
                    "No working backend implementation"
            );
            case PROGRESS_NOTES -> ok("Progress Notes / Additional Notes", "/progress-notes");
            case IV_FLUID_ADMINISTRATION -> blocked(
                    "IV Fluid Administration",
                    "/iv-fluid-administration",
                    "No working backend implementation"
            );
            case DIETARY_REQUEST -> blocked(
                    "Dietary Request",
                    "/dietary-request",
                    "No working backend implementation"
            );
            case PEDIATRIC -> blocked("Pediatric", "/pediatric", "No working backend implementation");
            case GYNECOLOGY -> blocked("Gynecology", "", "No working backend implementation");
            case SPEECH_THERAPY -> blocked(
                    "Speech Therapy",
                    "/speech-therapy",
                    "No working backend implementation"
            );
            case REHABILITATION_PLAN -> blocked(
                    "Rehabilitation Plan",
                    "",
                    "No working backend implementation"
            );
            case OCCUPATIONAL_THERAPY -> blocked(
                    "Occupational Therapy",
                    "/occupational-therapy",
                    "No working backend implementation"
            );
            case PHYSIOTHERAPY_PLAN -> blocked(
                    "Physiotherapy Plan",
                    "/physiotherapy-plan",
                    "No working backend implementation"
            );
            case MEDICATION_ADMINISTRATION_RECORD -> blocked(
                    "Medication Administration Record",
                    "/medication-administration-record",
                    "No MAR patient-service implementation"
            );
            case CONTINUOUS_OBSERVATIONS -> blocked(
                    "Continuous Observations",
                    "/continuous-observation",
                    "No working backend implementation"
            );
            case DIALYSIS_REQUEST -> blocked(
                    "Dialysis Request",
                    "/dialysis-request",
                    "No request entity (Dialysis Session is separate and supported)"
            );
            case SLIDING_SCALE -> blocked("Sliding Scale", "/sliding-scale", "No working backend implementation");
            case POINT_OF_CARE_CESTS -> blocked(
                    "Point of Care Tests",
                    "",
                    "No working backend implementation"
            );
            case HOSPITAL_COURSE -> blocked("Hospital Course", "", "No working backend implementation");
            case CHILD_GROWTH -> blocked("Child Growth", "", "No working backend implementation");
            case FLACC_NEONATES_PAIN_ASSESSMENT -> ok(
                    "FLACC Neonates Pain Assessment",
                    "/FLACC-neonates-pain-assessment"
            );
            case UNIVERSAL_PAIN_ASSESSMENT -> blocked(
                    "Universal Pain Assessment",
                    "",
                    "No backend (Observation Pain Assessment is separate and supported)"
            );
            case PATIENT_RESTRAINT -> blocked("Patient Restraint", "", "No working backend implementation");
            case INFECTION_CONTROL -> blocked("Infection Control", "", "No working backend implementation");
            case SOFA -> blocked("SOFA", "", "No working backend implementation");
            case MEDICAL_CALCULATORS -> blocked("Medical Calculators", "", "No working backend implementation");
            case CPOE_RESULTS_MANAGER -> blocked("CPOE Results Manager", "", "No working backend implementation");
            case ICU -> blocked("ICU", "/icu", "UI shell only; no documentation entity");
            case PREVIOUS_MEASUREMENTS -> blocked(
                    "Previous Measurements",
                    "/previous-measurements",
                    "Read-only history view; authoring covered by Vital Signs / Body Measurements"
            );
            case ATTACHMENTS -> blocked(
                    "Attachments",
                    "/attachments",
                    "No MedicalSheets documentation API for completion validation"
            );
            case SERVICE_AND_PRODUCTS -> ok("Service & Products", "/service-products");
            case CHIEF_COMPLAIN -> ok("Chief Complain", "/chief-complain");
            case PHYSICAL_EXAMINATION -> blocked(
                    "Physical Examination",
                    "/physical-examination",
                    "UI saves structured areas via general-service "
                            + "/encounter/save-physical-exam-area (encounter_key). "
                            + "Not the same as PHYSICAL_EXAMINATION_SUMMARY "
                            + "(patient-service encounter.physicalExaminationSummery). "
                            + "No Long encounter-id bridge for validators"
            );
            case PAIN_ASSESSMENT -> ok("Pain Assessment", "/pain-assessment");
            case GENERAL_ASSESSMENT -> ok("General Assessment", "/general-assessment");
            case FUNCTIONAL_ASSESSMENT -> blocked(
                    "Functional Assessment",
                    "/functional-assessment",
                    "Working GS save/load: /encounter/save-functional-assessment and "
                            + "/encounter/functional-assessment-list (ap_functional_assessment, "
                            + "encounter_key). PatientEncounter has Long id only; no existing "
                            + "GS Feign client or key bridge—cannot validate without migrating GS data"
            );
            case REPOSITIONING -> blocked(
                    "Repositioning",
                    "/repositioning",
                    "Working GS save/load: /encounter/repositioning-list and save "
                            + "(ap_repositioning, encounter_key). No Long encounter-id bridge "
                            + "or GS Feign client for validators"
            );
            case MEDICATION_RECONCILIATION -> blocked(
                    "Medication Reconciliation",
                    "/medication-reconciliation",
                    "Working GS save/load: /encounter/save-medication-reconciliation and "
                            + "/encounter/medication-reconciliation-list "
                            + "(ap_medication_reconciliation, encounter_key). No Long "
                            + "encounter-id bridge or GS Feign client for validators"
            );
            case WOUND_CARE_DOCUMENTATION -> blocked(
                    "Wound Care Documentation",
                    "/wound-care-documentation",
                    "No working backend implementation"
            );
            case SERVICEANDPRODUCTS -> ok("Service And Product", "/service-and-products");
            case FORMS -> ok("Forms", "/form-template-use");
            case NURSE_ASSESSMENT -> ok("Nurse Assessment", "/nurse-assessment");
            case UCC_MEDICATION_ORDER -> ok("UCC Medication Order", "/ucc-medication-order");
            case PHYSICIAN_ASSESSMENT -> ok("Physician Assessment", "/physician-assessment");
            case DENTAL_PROCEDURES -> ok("Dental Procedures", "/dental-procedures");
            case NEPHROLOGY_ASSESSMENT -> ok("Nephrology Assessment", "/nephrology-assessment");
            case DIALYSIS_SESSION -> ok("Dialysis Session", "/dialysis-session");
        };
    }

    private static SheetInfo ok(String name, String path) {
        return new SheetInfo(name, path, null);
    }

    private static SheetInfo blocked(String name, String path, String reason) {
        return new SheetInfo(name, path, reason);
    }

    private record SheetInfo(String name, String path, String unsupportedReason) {
    }
}
