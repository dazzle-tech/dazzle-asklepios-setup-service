package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.SurveyText;
import com.dazzle.asklepios.repository.SurveyTextRepository;
import com.dazzle.asklepios.service.dto.surveyText.SurveyTextCreateDTO;
import com.dazzle.asklepios.service.dto.surveyText.SurveyTextUpdateDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class SurveyTextService {

    private static final Logger LOG = LoggerFactory.getLogger(SurveyTextService.class);
    private static final String ENTITY_NAME = "surveyText";

    private final SurveyTextRepository repository;

    public SurveyTextService(SurveyTextRepository repository) {
        this.repository = repository;
    }

    public SurveyText create(SurveyTextCreateDTO dto) {
        LOG.debug("Request to create SurveyText : {}", dto);
        String textKey = dto.textKey().trim().toUpperCase(Locale.ROOT);
        if (repository.existsByTextKey(textKey)) {
            throw new BadRequestAlertException("A text already exists with this key", ENTITY_NAME, "keyexists");
        }

        return repository.save(SurveyText.builder()
                .textKey(textKey)
                .textEn(dto.textEn().trim())
                .textAr(dto.textAr().trim())
                .isActive(true)
                .build());
    }

    public SurveyText update(Long id, SurveyTextUpdateDTO dto) {
        LOG.debug("Request to update SurveyText id={} with {}", id, dto);
        SurveyText entity = getById(id);
        entity.setTextEn(dto.textEn().trim());
        entity.setTextAr(dto.textAr().trim());
        return repository.save(entity);
    }

    /** An inactive text is not sent to the survey page, which then shows its own default */
    public SurveyText toggleIsActive(Long id) {
        LOG.debug("Request to toggle SurveyText isActive id={}", id);
        SurveyText entity = getById(id);
        entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Page<SurveyText> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<SurveyText> findByStatus(Boolean isActive, Pageable pageable) {
        return repository.findByIsActive(isActive, pageable);
    }

    /** Matches the key, the English text or the Arabic text */
    @Transactional(readOnly = true)
    public Page<SurveyText> findByText(String text, Pageable pageable) {
        String value = text.trim();
        return repository.findByTextKeyContainingIgnoreCaseOrTextEnContainingIgnoreCaseOrTextArContainingIgnoreCase(
                value, value, value, pageable);
    }

    private SurveyText getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Survey text not found: " + id, ENTITY_NAME, "notfound"));
    }
}
