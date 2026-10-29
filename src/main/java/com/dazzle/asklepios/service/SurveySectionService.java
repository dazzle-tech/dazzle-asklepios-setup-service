package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.SurveySection;
import com.dazzle.asklepios.repository.SurveySectionRepository;
import com.dazzle.asklepios.service.dto.surveySection.SurveySectionDTO;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SurveySectionService {

    private static final Logger LOG = LoggerFactory.getLogger(SurveySectionService.class);
    private static final String ENTITY_NAME = "surveySection";

    private final SurveySectionRepository repository;

    public SurveySectionService(SurveySectionRepository repository) {
        this.repository = repository;
    }

    public SurveySection create(SurveySectionDTO dto) {
        LOG.debug("Request to create SurveySection : {}", dto);

        SurveySection entity = SurveySection.builder()
                .nameEn(dto.nameEn().trim())
                .nameAr(dto.nameAr().trim())
                .displayOrder(dto.displayOrder())
                .isActive(true)
                .build();

        return repository.save(entity);
    }

    public SurveySection update(Long id, SurveySectionDTO dto) {
        LOG.debug("Request to update SurveySection id={} with {}", id, dto);
        SurveySection entity = getById(id);
        entity.setNameEn(dto.nameEn().trim());
        entity.setNameAr(dto.nameAr().trim());
        entity.setDisplayOrder(dto.displayOrder());

        return repository.save(entity);
    }

    public SurveySection toggleIsActive(Long id) {
        LOG.debug("Request to toggle SurveySection isActive id={}", id);
        SurveySection entity = getById(id);
        entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Page<SurveySection> findAll(Pageable pageable) {
        return repository.findAllByOrderByDisplayOrderAscIdAsc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<SurveySection> findByStatus(Boolean isActive, Pageable pageable) {
        return repository.findByIsActiveOrderByDisplayOrderAscIdAsc(isActive, pageable);
    }

    /** Matches the text in the English or the Arabic name */
    @Transactional(readOnly = true)
    public Page<SurveySection> findByName(String name, Pageable pageable) {
        String text = name.trim();
        return repository.findByNameEnContainingIgnoreCaseOrNameArContainingIgnoreCaseOrderByDisplayOrderAscIdAsc(
                text, text, pageable);
    }

    @Transactional(readOnly = true)
    public SurveySection getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Survey section not found: " + id, ENTITY_NAME, "notfound"));
    }
}
