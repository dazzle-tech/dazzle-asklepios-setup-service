package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.SurveyQuestion;
import com.dazzle.asklepios.domain.SurveySection;
import com.dazzle.asklepios.domain.enumeration.SurveyAnswerType;
import com.dazzle.asklepios.repository.SurveyQuestionRepository;
import com.dazzle.asklepios.service.dto.surveyQuestion.SurveyQuestionCreateDTO;
import com.dazzle.asklepios.service.dto.surveyQuestion.SurveyQuestionUpdateDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SurveyQuestionService {

    private static final Logger LOG = LoggerFactory.getLogger(SurveyQuestionService.class);
    private static final String ENTITY_NAME = "surveyQuestion";


    private final SurveyQuestionRepository repository;
    private final SurveySectionService sectionService;

    public SurveyQuestionService(SurveyQuestionRepository repository, SurveySectionService sectionService) {
        this.repository = repository;
        this.sectionService = sectionService;
    }

    public SurveyQuestion create(SurveyQuestionCreateDTO dto) {
        LOG.debug("Request to create SurveyQuestion : {}", dto);

        SurveySection section = sectionService.getById(dto.sectionId());
        if (!Boolean.TRUE.equals(section.getIsActive())) {
            throw new BadRequestAlertException(
                    "Questions cannot be added to an inactive section", ENTITY_NAME, "sectioninactive");
        }

        SurveyQuestion entity = SurveyQuestion.builder()
                .sectionId(dto.sectionId())
                .displayOrder(dto.displayOrder())
                .questionEn(dto.questionEn().trim())
                .questionAr(dto.questionAr().trim())
                .answerType(dto.answerType())
                .isRequired(Boolean.TRUE.equals(dto.isRequired()))
                .isActive(true)
                .build();

        // Only SCALE has limits, TEXT keeps them empty
        if (dto.answerType() == SurveyAnswerType.SCALE) {
            if (dto.minValue() == null || dto.maxValue() == null) {
                throw new BadRequestAlertException(
                        "Min value and max value are required for a scale question", ENTITY_NAME, "scalerangerequired");
            }
            int minValue = dto.minValue();
            int maxValue = dto.maxValue();
            if (minValue >= maxValue) {
                throw new BadRequestAlertException(
                        "Min value must be less than max value", ENTITY_NAME, "scalerangeinvalid");
            }
            entity.setMinValue(minValue);
            entity.setMaxValue(maxValue);
        }

        return repository.save(entity);
    }

    /** Only the order and whether the question is required can change; the text and the answer stay fixed */
    public SurveyQuestion update(Long id, SurveyQuestionUpdateDTO dto) {
        LOG.debug("Request to update SurveyQuestion id={} with {}", id, dto);
        SurveyQuestion entity = getById(id);
        entity.setDisplayOrder(dto.displayOrder());
        entity.setIsRequired(dto.isRequired());
        return repository.save(entity);
    }

    public SurveyQuestion toggleIsActive(Long id) {
        LOG.debug("Request to toggle SurveyQuestion isActive id={}", id);
        SurveyQuestion entity = getById(id);
        entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Page<SurveyQuestion> findBySection(Long sectionId, Pageable pageable) {
        return repository.findBySectionIdOrderByDisplayOrderAscIdAsc(sectionId, pageable);
    }

    private SurveyQuestion getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Survey question not found: " + id, ENTITY_NAME, "notfound"));
    }
}
