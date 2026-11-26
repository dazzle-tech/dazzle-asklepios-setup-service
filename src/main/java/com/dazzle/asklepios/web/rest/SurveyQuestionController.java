package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.SurveyQuestion;
import com.dazzle.asklepios.service.SurveyQuestionService;
import com.dazzle.asklepios.service.dto.surveyQuestion.SurveyQuestionCreateDTO;
import com.dazzle.asklepios.service.dto.surveyQuestion.SurveyQuestionUpdateDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.surveyQuestion.SurveyQuestionResponseVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Setup of the patient satisfaction survey questions.
 * The text and the answer of a question are not editable: only its order and isRequired change, or it is deactivated.
 */
@RestController
@RequestMapping("/api/setup")
public class SurveyQuestionController {

    private static final Logger LOG = LoggerFactory.getLogger(SurveyQuestionController.class);
    private final SurveyQuestionService service;

    public SurveyQuestionController(SurveyQuestionService service) {
        this.service = service;
    }

    @PostMapping("/survey-question")
    public ResponseEntity<SurveyQuestionResponseVM> create(@Valid @RequestBody SurveyQuestionCreateDTO dto) {
        LOG.debug("REST create SurveyQuestion payload={}", dto);
        SurveyQuestion saved = service.create(dto);
        return ResponseEntity
                .created(URI.create("/api/setup/survey-question/" + saved.getId()))
                .body(SurveyQuestionResponseVM.ofEntity(saved));
    }

    /** Changes only the order and isRequired: the text and the answer of a question stay fixed */
    @PutMapping("/survey-question/{id}")
    public ResponseEntity<SurveyQuestionResponseVM> update(
            @PathVariable Long id,
            @Valid @RequestBody SurveyQuestionUpdateDTO dto
    ) {
        LOG.debug("REST update SurveyQuestion id={} payload={}", id, dto);
        return ResponseEntity.ok(SurveyQuestionResponseVM.ofEntity(service.update(id, dto)));
    }

    @PatchMapping("/survey-question/{id}/toggle-active")
    public ResponseEntity<SurveyQuestionResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle SurveyQuestion isActive id={}", id);
        return ResponseEntity.ok(SurveyQuestionResponseVM.ofEntity(service.toggleIsActive(id)));
    }

    /** GET the questions of one section (paged) */
    @GetMapping("/survey-question/by-section/{sectionId}")
    public ResponseEntity<List<SurveyQuestionResponseVM>> getBySection(
            @PathVariable Long sectionId,
            @ParameterObject Pageable pageable
    ) {
        Page<SurveyQuestion> page = service.findBySection(sectionId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(
                page.getContent().stream().map(SurveyQuestionResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }
}
