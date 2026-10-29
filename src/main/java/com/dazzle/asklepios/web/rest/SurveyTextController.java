package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.SurveyText;
import com.dazzle.asklepios.service.SurveyTextService;
import com.dazzle.asklepios.service.dto.surveyText.SurveyTextCreateDTO;
import com.dazzle.asklepios.service.dto.surveyText.SurveyTextUpdateDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.surveyText.SurveyTextResponseVM;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** Setup of the texts shown on the patient satisfaction survey page */
@RestController
@RequestMapping("/api/setup")
public class SurveyTextController {

    private static final Logger LOG = LoggerFactory.getLogger(SurveyTextController.class);
    private final SurveyTextService service;

    public SurveyTextController(SurveyTextService service) {
        this.service = service;
    }

    @PostMapping("/survey-text")
    public ResponseEntity<SurveyTextResponseVM> create(@Valid @RequestBody SurveyTextCreateDTO dto) {
        LOG.debug("REST create SurveyText payload={}", dto);
        SurveyText saved = service.create(dto);
        return ResponseEntity
                .created(URI.create("/api/setup/survey-text/" + saved.getId()))
                .body(SurveyTextResponseVM.ofEntity(saved));
    }

    @PutMapping("/survey-text/{id}")
    public ResponseEntity<SurveyTextResponseVM> update(
            @PathVariable Long id,
            @Valid @RequestBody SurveyTextUpdateDTO dto
    ) {
        LOG.debug("REST update SurveyText id={} payload={}", id, dto);
        return ResponseEntity.ok(SurveyTextResponseVM.ofEntity(service.update(id, dto)));
    }

    @PatchMapping("/survey-text/{id}/toggle-active")
    public ResponseEntity<SurveyTextResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle SurveyText isActive id={}", id);
        return ResponseEntity.ok(SurveyTextResponseVM.ofEntity(service.toggleIsActive(id)));
    }

    /** GET all (paged) */
    @GetMapping("/survey-text")
    public ResponseEntity<List<SurveyTextResponseVM>> getAll(@ParameterObject Pageable pageable) {
        return paged(service.findAll(pageable));
    }

    /** GET by status (paged) */
    @GetMapping("/survey-text/by-status/{isActive}")
    public ResponseEntity<List<SurveyTextResponseVM>> getByStatus(
            @PathVariable Boolean isActive,
            @ParameterObject Pageable pageable
    ) {
        return paged(service.findByStatus(isActive, pageable));
    }

    /** GET by key or text, English or Arabic (paged) */
    @GetMapping("/survey-text/by-text")
    public ResponseEntity<List<SurveyTextResponseVM>> getByText(
            @RequestParam String text,
            @ParameterObject Pageable pageable
    ) {
        return paged(service.findByText(text, pageable));
    }

    private static ResponseEntity<List<SurveyTextResponseVM>> paged(Page<SurveyText> page) {
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(
                page.getContent().stream().map(SurveyTextResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }
}
