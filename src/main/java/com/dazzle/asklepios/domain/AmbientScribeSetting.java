package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.AmbientAudioRetention;
import com.dazzle.asklepios.domain.enumeration.AmbientInstructionLanguage;
import com.dazzle.asklepios.domain.enumeration.AmbientNoteForm;
import com.dazzle.asklepios.domain.enumeration.AmbientNoteLanguage;
import com.dazzle.asklepios.domain.enumeration.AmbientNoteStyle;
import com.dazzle.asklepios.domain.enumeration.AmbientRecordingAccess;
import com.dazzle.asklepios.domain.enumeration.AmbientSpecialty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ambient_scribe_setting")
public class AmbientScribeSetting extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "note_language", nullable = false, length = 50)
    private AmbientNoteLanguage noteLanguage = AmbientNoteLanguage.ENGLISH;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "instruction_language", nullable = false, length = 50)
    private AmbientInstructionLanguage instructionLanguage = AmbientInstructionLanguage.PATIENT_PREFERRED;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "note_style", nullable = false, length = 50)
    private AmbientNoteStyle noteStyle = AmbientNoteStyle.BRIEF;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "specialty", nullable = false, length = 50)
    private AmbientSpecialty specialty = AmbientSpecialty.GENERAL_PRACTICE;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "note_form", nullable = false, length = 50)
    private AmbientNoteForm noteForm = AmbientNoteForm.PROSE;

    @NotNull
    @Column(name = "suggest_icd10", nullable = false)
    private Boolean suggestIcd10 = true;

    @NotNull
    @Column(name = "suggest_orders", nullable = false)
    private Boolean suggestOrders = true;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "audio_retention", nullable = false, length = 50)
    private AmbientAudioRetention audioRetention = AmbientAudioRetention.DAYS_30;

    @NotNull
    @Column(name = "consent_verbal", nullable = false)
    private Boolean consentVerbal = true;

    @NotNull
    @Column(name = "consent_tablet", nullable = false)
    private Boolean consentTablet = true;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "recording_access", nullable = false, length = 50)
    private AmbientRecordingAccess recordingAccess = AmbientRecordingAccess.TREATING_PHYSICIAN_AND_QA;

    @NotNull
    @Column(name = "allow_anonymized_audio", nullable = false)
    private Boolean allowAnonymizedAudio = false;

    @OneToMany(mappedBy = "setting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AmbientScribeClinic> clinics = new ArrayList<>();
}
