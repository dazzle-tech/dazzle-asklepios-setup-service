package com.dazzle.asklepios.web.rest.vm.ambientScribe;

import com.dazzle.asklepios.domain.enumeration.AmbientAudioRetention;
import com.dazzle.asklepios.domain.enumeration.AmbientInstructionLanguage;
import com.dazzle.asklepios.domain.enumeration.AmbientNoteForm;
import com.dazzle.asklepios.domain.enumeration.AmbientNoteLanguage;
import com.dazzle.asklepios.domain.enumeration.AmbientNoteStyle;
import com.dazzle.asklepios.domain.enumeration.AmbientRecordingAccess;
import com.dazzle.asklepios.domain.enumeration.AmbientSpecialty;

import java.util.List;

public record AmbientScribeSettingVM(
    Long id,
    Long facilityId,
    AmbientNoteLanguage noteLanguage,
    AmbientInstructionLanguage instructionLanguage,
    AmbientNoteStyle noteStyle,
    AmbientSpecialty specialty,
    AmbientNoteForm noteForm,
    Boolean suggestIcd10,
    Boolean suggestOrders,
    AmbientAudioRetention audioRetention,
    Boolean consentVerbal,
    Boolean consentTablet,
    AmbientRecordingAccess recordingAccess,
    Boolean allowAnonymizedAudio,
    List<AmbientScribeClinicVM> clinics
) {
}
