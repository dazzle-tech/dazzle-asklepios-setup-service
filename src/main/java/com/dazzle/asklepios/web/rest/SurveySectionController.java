package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.SurveySection;
import com.dazzle.asklepios.service.SurveySectionService;
import com.dazzle.asklepios.service.dto.surveySection.SurveySectionDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.surveySection.SurveySectionResponseVM;
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

/** Setup of the patient satisfaction survey sections */
@RestController
@RequestMapping("/api/setup")
public class SurveySectionController {

    private static final Logger LOG = LoggerFactory.getLogger(SurveySectionController.class);
    private final SurveySectionService service;

    public SurveySectionController(SurveySectionService service) {
        this.service = service;
    }

    @PostMapping("/survey-section")
    public ResponseEntity<SurveySectionResponseVM> create(@Valid @RequestBody SurveySectionDTO dto) {
        LOG.debug("REST create SurveySection payload={}", dto);
        SurveySection saved = service.create(dto);
        return ResponseEntity
                .created(URI.create("/api/setup/survey-section/" + saved.getId()))
                .body(SurveySectionResponseVM.ofEntity(saved));
    }

    @PutMapping("/survey-section/{id}")
    public ResponseEntity<SurveySectionResponseVM> update(
            @PathVariable Long id,
            @Valid @RequestBody SurveySectionDTO dto
    ) {
        LOG.debug("REST update SurveySection id={} payload={}", id, dto);
        return ResponseEntity.ok(SurveySectionResponseVM.ofEntity(service.update(id, dto)));
    }

    @PatchMapping("/survey-section/{id}/toggle-active")
    public ResponseEntity<SurveySectionResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle SurveySection isActive id={}", id);
        return ResponseEntity.ok(SurveySectionResponseVM.ofEntity(service.toggleIsActive(id)));
    }

    /** GET all (paged) */
    @GetMapping("/survey-section")
    public ResponseEntity<List<SurveySectionResponseVM>> getAll(@ParameterObject Pageable pageable) {
        return paged(service.findAll(pageable));
    }

    /** GET by status (paged) */
    @GetMapping("/survey-section/by-status/{isActive}")
    public ResponseEntity<List<SurveySectionResponseVM>> getByStatus(
            @PathVariable Boolean isActive,
            @ParameterObject Pageable pageable
    ) {
        return paged(service.findByStatus(isActive, pageable));
    }

    /** GET by name, English or Arabic (paged) */
    @GetMapping("/survey-section/by-name")
    public ResponseEntity<List<SurveySectionResponseVM>> getByName(
            @RequestParam String name,
            @ParameterObject Pageable pageable
    ) {
        return paged(service.findByName(name, pageable));
    }

    private static ResponseEntity<List<SurveySectionResponseVM>> paged(Page<SurveySection> page) {
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(
                page.getContent().stream().map(SurveySectionResponseVM::ofEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }
}
