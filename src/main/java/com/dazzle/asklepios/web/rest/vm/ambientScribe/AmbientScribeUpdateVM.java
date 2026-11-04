package com.dazzle.asklepios.web.rest.vm.ambientScribe;

import com.dazzle.asklepios.domain.enumeration.AmbientAudioRetention;
import com.dazzle.asklepios.domain.enumeration.AmbientInstructionLanguage;
import com.dazzle.asklepios.domain.enumeration.AmbientNoteForm;
import com.dazzle.asklepios.domain.enumeration.AmbientNoteLanguage;
import com.dazzle.asklepios.domain.enumeration.AmbientNoteStyle;
import com.dazzle.asklepios.domain.enumeration.AmbientRecordingAccess;
import com.dazzle.asklepios.domain.enumeration.AmbientSpecialty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AmbientScribeUpdateVM(
    @NotNull AmbientNoteLanguage noteLanguage,
    @NotNull AmbientInstructionLanguage instructionLanguage,
    @NotNull AmbientNoteStyle noteStyle,
    @NotNull AmbientSpecialty specialty,
    @NotNull AmbientNoteForm noteForm,
    @NotNull Boolean suggestIcd10,
    @NotNull Boolean suggestOrders,
    @NotNull AmbientAudioRetention audioRetention,
    @NotNull Boolean consentVerbal,
    @NotNull Boolean consentTablet,
    @NotNull AmbientRecordingAccess recordingAccess,
    @NotNull Boolean allowAnonymizedAudio,
    @NotNull @Valid List<AmbientScribeClinicUpdateVM> clinics
) {
}
