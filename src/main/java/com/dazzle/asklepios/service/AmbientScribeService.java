package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.AmbientScribeClinic;
import com.dazzle.asklepios.domain.AmbientScribeClinicPractitioner;
import com.dazzle.asklepios.domain.AmbientScribeSetting;
import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.domain.PractitionerDepartment;
import com.dazzle.asklepios.domain.enumeration.AmbientAudioRetention;
import com.dazzle.asklepios.domain.enumeration.AmbientInstructionLanguage;
import com.dazzle.asklepios.domain.enumeration.AmbientNoteForm;
import com.dazzle.asklepios.domain.enumeration.AmbientNoteLanguage;
import com.dazzle.asklepios.domain.enumeration.AmbientNoteStyle;
import com.dazzle.asklepios.domain.enumeration.AmbientRecordingAccess;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.AmbientSpecialty;
import com.dazzle.asklepios.repository.AmbientScribeSettingRepository;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.PractitionerDepartmentRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.ambientScribe.AmbientScribeClinicUpdateVM;
import com.dazzle.asklepios.web.rest.vm.ambientScribe.AmbientScribeClinicVM;
import com.dazzle.asklepios.web.rest.vm.ambientScribe.AmbientScribePractitionerVM;
import com.dazzle.asklepios.web.rest.vm.ambientScribe.AmbientScribeSettingVM;
import com.dazzle.asklepios.web.rest.vm.ambientScribe.AmbientScribeUpdateVM;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class AmbientScribeService {

    private static final String ENTITY_NAME = "ambientScribe";

    private final AmbientScribeSettingRepository settingRepository;
    private final FacilityRepository facilityRepository;
    private final DepartmentsRepository departmentsRepository;
    private final PractitionerDepartmentRepository practitionerDepartmentRepository;

    public AmbientScribeService(
        AmbientScribeSettingRepository settingRepository,
        FacilityRepository facilityRepository,
        DepartmentsRepository departmentsRepository,
        PractitionerDepartmentRepository practitionerDepartmentRepository
    ) {
        this.settingRepository = settingRepository;
        this.facilityRepository = facilityRepository;
        this.departmentsRepository = departmentsRepository;
        this.practitionerDepartmentRepository = practitionerDepartmentRepository;
    }

    @Transactional(readOnly = true)
    public AmbientScribeSettingVM getByFacility(Long facilityId) {
        Facility facility = loadFacility(facilityId);
        List<Department> departments = activeClinics(facilityId);
        AmbientScribeSetting setting = settingRepository.findAllByFacilityId(facilityId).orElse(null);
        return toVm(facility, setting, departments);
    }

    public AmbientScribeSettingVM save(Long facilityId, AmbientScribeUpdateVM update) {
        Facility facility = loadFacility(facilityId);
        List<Department> departments = activeClinics(facilityId);
        Map<Long, Department> departmentsById = new HashMap<>();
        for (Department department : departments) {
            departmentsById.put(department.getId(), department);
        }

        AmbientScribeSetting setting = getOrCreate(facility);
        applySettings(setting, update);

        Map<Long, AmbientScribeClinic> clinicsByDepartment = new HashMap<>();
        for (AmbientScribeClinic clinic : setting.getClinics()) {
            clinicsByDepartment.put(clinic.getDepartment().getId(), clinic);
        }

        for (AmbientScribeClinicUpdateVM clinicUpdate : update.clinics()) {
            Department department = departmentsById.get(clinicUpdate.departmentId());
            if (department == null) {
                throw new BadRequestAlertException(
                    "Clinic does not belong to this facility",
                    ENTITY_NAME,
                    "department.invalid"
                );
            }

            AmbientScribeClinic clinic = clinicsByDepartment.get(department.getId());
            if (clinic == null) {
                clinic = new AmbientScribeClinic();
                clinic.setSetting(setting);
                clinic.setDepartment(department);
                setting.getClinics().add(clinic);
                clinicsByDepartment.put(department.getId(), clinic);
            }

            clinic.setEnabled(Boolean.TRUE.equals(clinicUpdate.enabled()));
            replacePractitioners(clinic, department, clinicUpdate.practitionerIds());
        }

        settingRepository.saveAndFlush(setting);
        return getByFacility(facilityId);
    }

    public AmbientScribeSettingVM setClinicsEnabled(Long facilityId, boolean enabled) {
        Facility facility = loadFacility(facilityId);
        List<Department> departments = activeClinics(facilityId);
        AmbientScribeSetting setting = getOrCreate(facility);

        Map<Long, AmbientScribeClinic> clinicsByDepartment = new HashMap<>();
        for (AmbientScribeClinic clinic : setting.getClinics()) {
            clinicsByDepartment.put(clinic.getDepartment().getId(), clinic);
        }

        for (Department department : departments) {
            AmbientScribeClinic clinic = clinicsByDepartment.get(department.getId());
            if (clinic == null) {
                clinic = new AmbientScribeClinic();
                clinic.setSetting(setting);
                clinic.setDepartment(department);
                setting.getClinics().add(clinic);
            }

            clinic.setEnabled(enabled);
        }

        settingRepository.saveAndFlush(setting);
        return getByFacility(facilityId);
    }

    private AmbientScribeSetting getOrCreate(Facility facility) {
        return settingRepository
            .findAllByFacilityId(facility.getId())
            .orElseGet(() -> {
                AmbientScribeSetting created = defaultSetting(facility);
                return settingRepository.saveAndFlush(created);
            });
    }

    private void applySettings(AmbientScribeSetting setting, AmbientScribeUpdateVM update) {
        setting.setNoteLanguage(update.noteLanguage());
        setting.setInstructionLanguage(update.instructionLanguage());
        setting.setNoteStyle(update.noteStyle());
        setting.setSpecialty(update.specialty());
        setting.setNoteForm(update.noteForm());
        setting.setSuggestIcd10(update.suggestIcd10());
        setting.setSuggestOrders(update.suggestOrders());
        setting.setAudioRetention(update.audioRetention());
        setting.setConsentVerbal(update.consentVerbal());
        setting.setConsentTablet(update.consentTablet());
        setting.setRecordingAccess(update.recordingAccess());
        setting.setAllowAnonymizedAudio(update.allowAnonymizedAudio());
    }

    private void replacePractitioners(AmbientScribeClinic clinic, Department department, List<Long> practitionerIds) {
        List<PractitionerDepartment> links = practitionerDepartmentRepository
            .findByDepartmentIdAndPractitioner_IsActiveIsTrue(department.getId());
        Map<Long, Practitioner> practitionersById = new HashMap<>();
        for (PractitionerDepartment link : links) {
            if (link.getPractitioner() != null && link.getPractitioner().getId() != null) {
                practitionersById.put(link.getPractitioner().getId(), link.getPractitioner());
            }
        }

        Set<Long> requested = new HashSet<>(practitionerIds == null ? List.of() : practitionerIds);
        for (Long practitionerId : requested) {
            if (!practitionersById.containsKey(practitionerId)) {
                throw new BadRequestAlertException(
                    "Physician is not assigned to this clinic",
                    ENTITY_NAME,
                    "practitioner.invalid"
                );
            }
        }

        if (!clinic.getPractitioners().isEmpty()) {
            clinic.getPractitioners().clear();
            if (clinic.getId() != null) {
                settingRepository.saveAndFlush(clinic.getSetting());
            }
        }
        for (Long practitionerId : requested) {
            AmbientScribeClinicPractitioner assignment = new AmbientScribeClinicPractitioner();
            assignment.setClinic(clinic);
            assignment.setPractitioner(practitionersById.get(practitionerId));
            clinic.getPractitioners().add(assignment);
        }
    }

    private AmbientScribeSettingVM toVm(Facility facility, AmbientScribeSetting setting, List<Department> departments) {
        Map<Long, AmbientScribeClinic> clinicsByDepartment = new HashMap<>();
        if (setting != null) {
            for (AmbientScribeClinic clinic : setting.getClinics()) {
                if (clinic.getDepartment() != null) {
                    clinicsByDepartment.put(clinic.getDepartment().getId(), clinic);
                }
            }
        }

        List<AmbientScribeClinicVM> clinics = new ArrayList<>();
        for (Department department : departments) {
            clinics.add(toClinicVm(department, clinicsByDepartment.get(department.getId())));
        }

        if (setting == null) {
            AmbientScribeSetting defaults = defaultSetting(facility);
            return new AmbientScribeSettingVM(
                null,
                facility.getId(),
                defaults.getNoteLanguage(),
                defaults.getInstructionLanguage(),
                defaults.getNoteStyle(),
                defaults.getSpecialty(),
                defaults.getNoteForm(),
                defaults.getSuggestIcd10(),
                defaults.getSuggestOrders(),
                defaults.getAudioRetention(),
                defaults.getConsentVerbal(),
                defaults.getConsentTablet(),
                defaults.getRecordingAccess(),
                defaults.getAllowAnonymizedAudio(),
                clinics
            );
        }

        return new AmbientScribeSettingVM(
            setting.getId(),
            facility.getId(),
            setting.getNoteLanguage(),
            setting.getInstructionLanguage(),
            setting.getNoteStyle(),
            setting.getSpecialty(),
            setting.getNoteForm(),
            setting.getSuggestIcd10(),
            setting.getSuggestOrders(),
            setting.getAudioRetention(),
            setting.getConsentVerbal(),
            setting.getConsentTablet(),
            setting.getRecordingAccess(),
            setting.getAllowAnonymizedAudio(),
            clinics
        );
    }

    private AmbientScribeClinicVM toClinicVm(Department department, AmbientScribeClinic clinic) {
        Set<Long> enabledIds = new HashSet<>();
        if (clinic != null) {
            for (AmbientScribeClinicPractitioner assignment : clinic.getPractitioners()) {
                if (assignment.getPractitioner() != null && assignment.getPractitioner().getId() != null) {
                    enabledIds.add(assignment.getPractitioner().getId());
                }
            }
        }

        List<AmbientScribePractitionerVM> practitioners = new ArrayList<>();
        for (PractitionerDepartment link : practitionerDepartmentRepository
            .findByDepartmentIdAndPractitioner_IsActiveIsTrue(department.getId())) {
            Practitioner practitioner = link.getPractitioner();
            if (practitioner == null || practitioner.getId() == null) {
                continue;
            }
            practitioners.add(
                new AmbientScribePractitionerVM(
                    practitioner.getId(),
                    practitionerName(practitioner),
                    enabledIds.contains(practitioner.getId())
                )
            );
        }

        return new AmbientScribeClinicVM(
            clinic == null ? null : clinic.getId(),
            department.getId(),
            department.getName(),
            clinic != null && Boolean.TRUE.equals(clinic.getEnabled()),
            practitioners
        );
    }

    private AmbientScribeSetting defaultSetting(Facility facility) {
        AmbientScribeSetting setting = new AmbientScribeSetting();
        setting.setFacility(facility);
        setting.setNoteLanguage(AmbientNoteLanguage.ENGLISH);
        setting.setInstructionLanguage(AmbientInstructionLanguage.PATIENT_PREFERRED);
        setting.setNoteStyle(AmbientNoteStyle.BRIEF);
        setting.setSpecialty(AmbientSpecialty.GENERAL_PRACTICE);
        setting.setNoteForm(AmbientNoteForm.PROSE);
        setting.setSuggestIcd10(true);
        setting.setSuggestOrders(true);
        setting.setAudioRetention(AmbientAudioRetention.DAYS_30);
        setting.setConsentVerbal(true);
        setting.setConsentTablet(true);
        setting.setRecordingAccess(AmbientRecordingAccess.TREATING_PHYSICIAN_AND_QA);
        setting.setAllowAnonymizedAudio(false);
        return setting;
    }

    private String practitionerName(Practitioner practitioner) {
        String first = practitioner.getFirstName() == null ? "" : practitioner.getFirstName().trim();
        String last = practitioner.getLastName() == null ? "" : practitioner.getLastName().trim();
        String name = (first + " " + last).trim();
        return name.isEmpty() ? "Physician " + practitioner.getId() : name;
    }

    private List<Department> activeClinics(Long facilityId) {
        return departmentsRepository.findByTypeAndFacilityIdAndIsActiveTrue(DepartmentType.OUTPATIENT_CLINIC, facilityId);
    }

    private Facility loadFacility(Long facilityId) {
        return facilityRepository
            .findById(facilityId)
            .orElseThrow(() -> new NotFoundAlertException("Facility not found", ENTITY_NAME, "facility.notfound"));
    }
}
