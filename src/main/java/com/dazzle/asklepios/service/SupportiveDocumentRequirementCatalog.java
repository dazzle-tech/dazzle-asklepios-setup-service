package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.enumeration.MedicalSheets;
import com.dazzle.asklepios.domain.enumeration.SupportiveDocumentRequirementCode;
import com.dazzle.asklepios.web.rest.vm.supportivedocument.SupportiveDocumentMedicalSheetRefVM;
import com.dazzle.asklepios.web.rest.vm.supportivedocument.SupportiveDocumentRequirementCatalogVM;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Authoritative metadata for supportive-document requirement codes.
 * Parent groups use the existing {@link MedicalSheets} enum values.
 * {@code additionalMedicalSheets} lists further groups that display the same code.
 */
@Component
public class SupportiveDocumentRequirementCatalog {

    private static final Map<SupportiveDocumentRequirementCode, CatalogEntry> ENTRIES = Map.ofEntries(
            entry(
                    SupportiveDocumentRequirementCode.CHIEF_COMPLAINT,
                    "Chief Complaint",
                    MedicalSheets.CLINICAL_VISIT,
                    1,
                    MedicalSheets.PHYSICIAN_ASSESSMENT
            ),
            entry(
                    SupportiveDocumentRequirementCode.HISTORY_OF_PRESENT_ILLNESS,
                    "History Of Present Illness",
                    MedicalSheets.CLINICAL_VISIT,
                    2,
                    MedicalSheets.PHYSICIAN_ASSESSMENT
            ),
            entry(
                    SupportiveDocumentRequirementCode.PHYSICAL_EXAMINATION_SUMMARY,
                    "Physical Examination Summary",
                    MedicalSheets.CLINICAL_VISIT,
                    3,
                    MedicalSheets.PHYSICIAN_ASSESSMENT
            ),
            entry(
                    SupportiveDocumentRequirementCode.REVIEW_OF_SYSTEMS,
                    "Physical Examination & Findings (Review of Systems)",
                    MedicalSheets.CLINICAL_VISIT,
                    4
            ),
            entry(
                    SupportiveDocumentRequirementCode.PATIENT_DIAGNOSIS,
                    "Patient Diagnosis",
                    MedicalSheets.CLINICAL_VISIT,
                    5,
                    MedicalSheets.PHYSICIAN_ASSESSMENT
            ),
            entry(
                    SupportiveDocumentRequirementCode.ASSESSMENT,
                    "Assessment",
                    MedicalSheets.CLINICAL_VISIT,
                    6,
                    MedicalSheets.PHYSICIAN_ASSESSMENT
            ),
            entry(
                    SupportiveDocumentRequirementCode.TREATMENT_PLAN,
                    "Treatment Plan / Management",
                    MedicalSheets.CLINICAL_VISIT,
                    7,
                    MedicalSheets.PHYSICIAN_ASSESSMENT
            ),
            entry(
                    SupportiveDocumentRequirementCode.PATIENT_PROBLEMS,
                    "Patient's Problems",
                    MedicalSheets.PATIENT_HISTORY,
                    8,
                    MedicalSheets.NURSE_ASSESSMENT,
                    MedicalSheets.PHYSICIAN_ASSESSMENT,
                    MedicalSheets.CLINICAL_VISIT
            ),
            entry(
                    SupportiveDocumentRequirementCode.FAMILY_HISTORY,
                    "Family History",
                    MedicalSheets.PATIENT_HISTORY,
                    9,
                    MedicalSheets.NURSE_ASSESSMENT,
                    MedicalSheets.PHYSICIAN_ASSESSMENT,
                    MedicalSheets.CLINICAL_VISIT
            ),
            entry(
                    SupportiveDocumentRequirementCode.HOSPITALIZATIONS,
                    "Hospitalizations",
                    MedicalSheets.PATIENT_HISTORY,
                    10,
                    MedicalSheets.NURSE_ASSESSMENT,
                    MedicalSheets.PHYSICIAN_ASSESSMENT,
                    MedicalSheets.CLINICAL_VISIT
            ),
            entry(
                    SupportiveDocumentRequirementCode.CURRENT_MEDICATION,
                    "Current Medication",
                    MedicalSheets.PATIENT_HISTORY,
                    11,
                    MedicalSheets.NURSE_ASSESSMENT,
                    MedicalSheets.PHYSICIAN_ASSESSMENT,
                    MedicalSheets.CLINICAL_VISIT
            ),
            entry(
                    SupportiveDocumentRequirementCode.SURGICAL_HISTORY,
                    "Surgical History",
                    MedicalSheets.PATIENT_HISTORY,
                    12,
                    MedicalSheets.NURSE_ASSESSMENT,
                    MedicalSheets.PHYSICIAN_ASSESSMENT,
                    MedicalSheets.CLINICAL_VISIT
            ),
            entry(
                    SupportiveDocumentRequirementCode.SOCIAL_HISTORY,
                    "Social History",
                    MedicalSheets.PATIENT_HISTORY,
                    13,
                    MedicalSheets.NURSE_ASSESSMENT,
                    MedicalSheets.PHYSICIAN_ASSESSMENT,
                    MedicalSheets.CLINICAL_VISIT
            ),
            entry(
                    SupportiveDocumentRequirementCode.VITAL_SIGNS,
                    "Vital Signs",
                    MedicalSheets.OBSERVATION,
                    14,
                    MedicalSheets.NURSE_ASSESSMENT
            ),
            entry(
                    SupportiveDocumentRequirementCode.BODY_MEASUREMENTS,
                    "Body Measurements",
                    MedicalSheets.OBSERVATION,
                    15,
                    MedicalSheets.NURSE_ASSESSMENT
            ),
            entry(
                    SupportiveDocumentRequirementCode.PAIN_ASSESSMENT,
                    "Pain Assessment",
                    MedicalSheets.OBSERVATION,
                    16,
                    MedicalSheets.NURSE_ASSESSMENT,
                    MedicalSheets.PAIN_ASSESSMENT
            ),
            entry(
                    SupportiveDocumentRequirementCode.ADDITIONAL_MEASUREMENTS,
                    "Additional Measurements",
                    MedicalSheets.OBSERVATION,
                    17,
                    MedicalSheets.NURSE_ASSESSMENT
            ),
            entry(
                    SupportiveDocumentRequirementCode.PATIENT_OBSERVATIONS_COMPLAINTS,
                    "Patient Observations / Complaints",
                    MedicalSheets.OBSERVATION,
                    18,
                    MedicalSheets.NURSE_ASSESSMENT
            ),
            entry(
                    SupportiveDocumentRequirementCode.PROGRESS_NOTES,
                    "Progress Notes",
                    MedicalSheets.PROGRESS_NOTES,
                    19,
                    MedicalSheets.NURSE_ASSESSMENT,
                    MedicalSheets.PHYSICIAN_ASSESSMENT,
                    MedicalSheets.CLINICAL_VISIT
            ),
            entry(
                    SupportiveDocumentRequirementCode.GLASGOW_COMA_SCALE,
                    "Glasgow Coma Scale",
                    MedicalSheets.GLASGOW_COMA_SCALE,
                    20
            ),
            entry(
                    SupportiveDocumentRequirementCode.FLACC_NEONATES_PAIN_ASSESSMENT,
                    "FLACC Neonates Pain Assessment",
                    MedicalSheets.FLACC_NEONATES_PAIN_ASSESSMENT,
                    21
            ),
            entry(
                    SupportiveDocumentRequirementCode.DIALYSIS_SESSION,
                    "Dialysis Session",
                    MedicalSheets.DIALYSIS_SESSION,
                    22
            ),
            entry(
                    SupportiveDocumentRequirementCode.VACCINATION,
                    "Vaccination",
                    MedicalSheets.VACCINATION,
                    23,
                    MedicalSheets.VACCINE_RECCORD
            ),
            entry(
                    SupportiveDocumentRequirementCode.NEPHROLOGY_KIDNEY_ASSESSMENT,
                    "Nephrology Kidney Assessment",
                    MedicalSheets.NEPHROLOGY_ASSESSMENT,
                    24
            ),
            entry(
                    SupportiveDocumentRequirementCode.NEPHROLOGY_RENAL_FUNCTION,
                    "Nephrology Renal Function",
                    MedicalSheets.NEPHROLOGY_ASSESSMENT,
                    25
            ),
            entry(
                    SupportiveDocumentRequirementCode.NEPHROLOGY_TREATMENT_PLAN,
                    "Nephrology Treatment Plan",
                    MedicalSheets.NEPHROLOGY_ASSESSMENT,
                    26
            ),
            entry(
                    SupportiveDocumentRequirementCode.DENTAL_CARE,
                    "Dental Care Chart",
                    MedicalSheets.DENTAL_CARE,
                    27
            ),
            entry(
                    SupportiveDocumentRequirementCode.DENTAL_PROCEDURES,
                    "Dental Procedures",
                    MedicalSheets.DENTAL_PROCEDURES,
                    28
            ),
            entry(
                    SupportiveDocumentRequirementCode.FORMS,
                    "Forms",
                    MedicalSheets.FORMS,
                    29
            ),
            entry(
                    SupportiveDocumentRequirementCode.CHIEF_COMPLAIN,
                    "Chief Complain Record (standalone entity)",
                    MedicalSheets.CHIEF_COMPLAIN,
                    30
            ),
            entry(
                    SupportiveDocumentRequirementCode.GENERAL_ASSESSMENT,
                    "General Assessment",
                    MedicalSheets.GENERAL_ASSESSMENT,
                    31
            ),
            entry(
                    SupportiveDocumentRequirementCode.ALLERGIES,
                    "Allergy Record Documented (non-cancelled; not NKDA)",
                    MedicalSheets.ALLERGIES,
                    32,
                    MedicalSheets.CLINICAL_VISIT
            ),
            entry(
                    SupportiveDocumentRequirementCode.MEDICAL_WARNINGS,
                    "Medical Warning Record Documented (non-cancelled)",
                    MedicalSheets.MEDICAL_WARNINGS,
                    33
            ),
            entry(
                    SupportiveDocumentRequirementCode.CONSULTATION,
                    "Consultation Requested (not cancelled)",
                    MedicalSheets.CONSULTATION,
                    34
            ),
            entry(
                    SupportiveDocumentRequirementCode.PROCEDURES,
                    "Procedure Requested (not cancelled)",
                    MedicalSheets.PROCEDURES,
                    35
            ),
            entry(
                    SupportiveDocumentRequirementCode.REFERRAL_REQUEST,
                    "Referral Requested or Accepted",
                    MedicalSheets.REFERRAL_REQUEST,
                    36
            ),
            entry(
                    SupportiveDocumentRequirementCode.PRESCRIPTION,
                    "Prescription Submitted or Sent",
                    MedicalSheets.PRESCRIPTION,
                    37,
                    MedicalSheets.MEDICATIONS_RECORD
            ),
            entry(
                    SupportiveDocumentRequirementCode.DIAGNOSTICS_ORDER,
                    "Diagnostics Order Submitted (not draft)",
                    MedicalSheets.DIAGNOSTICS_ORDER,
                    38
            ),
            entry(
                    SupportiveDocumentRequirementCode.DIAGNOSTICS_RESULT,
                    "Diagnostics Result Documented",
                    MedicalSheets.DIAGNOSTICS_RESULT,
                    39
            ),
            entry(
                    SupportiveDocumentRequirementCode.UCC_MEDICATION_ORDER,
                    "UCC Medication Order Submitted or Later",
                    MedicalSheets.UCC_MEDICATION_ORDER,
                    40,
                    MedicalSheets.MEDICATIONS_RECORD
            ),
            entry(
                    SupportiveDocumentRequirementCode.SERVICEANDPRODUCTS,
                    "Service And Products Line (non-cancelled)",
                    MedicalSheets.SERVICEANDPRODUCTS,
                    41,
                    MedicalSheets.SERVICE_AND_PRODUCTS,
                    MedicalSheets.CLINICAL_VISIT
            )
    );

    public List<SupportiveDocumentRequirementCatalogVM> listAll() {
        return Arrays.stream(SupportiveDocumentRequirementCode.values())
                .map(ENTRIES::get)
                .map(CatalogEntry::toVm)
                .toList();
    }

    public boolean isKnown(SupportiveDocumentRequirementCode code) {
        return code != null && ENTRIES.containsKey(code);
    }

    private static Map.Entry<SupportiveDocumentRequirementCode, CatalogEntry> entry(
            SupportiveDocumentRequirementCode code,
            String label,
            MedicalSheets primarySheet,
            int displayOrder,
            MedicalSheets... additionalSheets
    ) {
        List<SupportiveDocumentMedicalSheetRefVM> additional =
                additionalSheets == null || additionalSheets.length == 0
                        ? List.of()
                        : Arrays.stream(additionalSheets)
                                .map(SupportiveDocumentRequirementCatalog::toSheetRef)
                                .toList();
        return Map.entry(
                code,
                new CatalogEntry(code, label, primarySheet, additional, displayOrder)
        );
    }

    private static SupportiveDocumentMedicalSheetRefVM toSheetRef(MedicalSheets sheet) {
        SheetMeta meta = sheetMeta(sheet);
        return new SupportiveDocumentMedicalSheetRefVM(sheet.name(), meta.name(), meta.path());
    }

    private static SheetMeta sheetMeta(MedicalSheets sheet) {
        return switch (sheet) {
            case CLINICAL_VISIT -> new SheetMeta("Clinical Visit", "/clinical-visit");
            case PHYSICIAN_ASSESSMENT -> new SheetMeta("Physician Assessment", "/physician-assessment");
            case PHYSICAL_EXAMINATION -> new SheetMeta("Physical Examination", "/physical-examination");
            case PATIENT_HISTORY -> new SheetMeta("Patient History", "/patient-history");
            case NURSE_ASSESSMENT -> new SheetMeta("Nurse Assessment", "/nurse-assessment");
            case MEDICATIONS_RECORD -> new SheetMeta("Medications Record", "/medications-record");
            case OBSERVATION -> new SheetMeta("Observation", "/observations");
            case PAIN_ASSESSMENT -> new SheetMeta("Pain Assessment", "/pain-assessment");
            case PROGRESS_NOTES -> new SheetMeta("Progress Notes / Additional Notes", "/progress-notes");
            case GLASGOW_COMA_SCALE -> new SheetMeta("Glasgow Coma Scale", "/glasgow-coma-scale");
            case FLACC_NEONATES_PAIN_ASSESSMENT -> new SheetMeta(
                    "FLACC Neonates Pain Assessment",
                    "/FLACC-neonates-pain-assessment"
            );
            case DIALYSIS_SESSION -> new SheetMeta("Dialysis Session", "/dialysis-session");
            case VACCINATION -> new SheetMeta("Vaccination", "/vaccination");
            case VACCINE_RECCORD -> new SheetMeta("Vaccine Record", "/vaccine-record");
            case NEPHROLOGY_ASSESSMENT -> new SheetMeta("Nephrology Assessment", "/nephrology-assessment");
            case DENTAL_CARE -> new SheetMeta("Dental Care", "/dental-care");
            case DENTAL_PROCEDURES -> new SheetMeta("Dental Procedures", "/dental-procedures");
            case FORMS -> new SheetMeta("Forms", "/form-template-use");
            case CHIEF_COMPLAIN -> new SheetMeta("Chief Complain", "/chief-complain");
            case GENERAL_ASSESSMENT -> new SheetMeta("General Assessment", "/general-assessment");
            case ALLERGIES -> new SheetMeta("Allergies", "/allergies");
            case MEDICAL_WARNINGS -> new SheetMeta("Medical Warnings", "/medical-warnings");
            case CONSULTATION -> new SheetMeta("Consultation", "/consultation");
            case PROCEDURES -> new SheetMeta("Procedures", "/procedures");
            case REFERRAL_REQUEST -> new SheetMeta("Referral Request", "/referral-request");
            case PRESCRIPTION -> new SheetMeta("Prescription", "/prescription");
            case DIAGNOSTICS_ORDER -> new SheetMeta("Diagnostics Order", "/diagnostics-order");
            case DIAGNOSTICS_RESULT -> new SheetMeta("Diagnostics Test Result", "/diagnostics-result");
            case UCC_MEDICATION_ORDER -> new SheetMeta("UCC Medication Order", "/ucc-medication-order");
            case SERVICEANDPRODUCTS -> new SheetMeta("Service And Product", "/service-and-products");
            case SERVICE_AND_PRODUCTS -> new SheetMeta("Service & Products", "/service-products");
            default -> throw new IllegalArgumentException("Unsupported medical sheet for catalog: " + sheet);
        };
    }

    private record SheetMeta(String name, String path) {
    }

    private record CatalogEntry(
            SupportiveDocumentRequirementCode code,
            String label,
            MedicalSheets medicalSheet,
            List<SupportiveDocumentMedicalSheetRefVM> additionalMedicalSheets,
            int displayOrder
    ) {
        SupportiveDocumentRequirementCatalogVM toVm() {
            SheetMeta meta = sheetMeta(medicalSheet);
            List<SupportiveDocumentMedicalSheetRefVM> additional =
                    additionalMedicalSheets == null ? List.of() : additionalMedicalSheets;
            return new SupportiveDocumentRequirementCatalogVM(
                    code.name(),
                    label,
                    medicalSheet.name(),
                    meta.name(),
                    meta.path(),
                    displayOrder,
                    additional
            );
        }
    }
}
