package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * A text shown on the patient satisfaction survey page (title, instructions, buttons, messages),
 * in English and Arabic. The page finds it by textKey; {placeholders} are filled by the page.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Table(name = "survey_text")
public class SurveyText extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Column(name = "text_key", nullable = false, length = 100, unique = true)
    private String textKey;

    @NotNull
    @Column(name = "text_en", nullable = false, columnDefinition = "TEXT")
    private String textEn;

    @NotNull
    @Column(name = "text_ar", nullable = false, columnDefinition = "TEXT")
    private String textAr;

    @NotNull
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
