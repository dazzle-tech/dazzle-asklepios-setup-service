package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.SurveyAnswerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * A patient satisfaction survey question.
 * SCALE questions use minValue/maxValue, TEXT questions have no limits.
 * The text and the answer are never edited after the question is created,
 * only its order can change and it can be deactivated.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Table(name = "survey_question")
public class SurveyQuestion extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    /** Order of the question inside its section */
    @NotNull
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @NotNull
    @Column(name = "question_en", nullable = false, length = 1000)
    private String questionEn;

    @NotNull
    @Column(name = "question_ar", nullable = false, length = 1000)
    private String questionAr;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "answer_type", nullable = false, length = 20)
    private SurveyAnswerType answerType;

    @Column(name = "min_value")
    private Integer minValue;

    @Column(name = "max_value")
    private Integer maxValue;

    /** False when the patient may skip the question */
    @NotNull
    @Builder.Default
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @NotNull
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
