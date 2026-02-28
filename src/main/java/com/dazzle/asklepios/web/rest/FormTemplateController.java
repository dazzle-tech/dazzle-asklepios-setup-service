package com.dazzle.asklepios.web.rest;


import com.dazzle.asklepios.domain.FormTemplate;
import com.dazzle.asklepios.service.FormTemplateService;
import com.dazzle.asklepios.service.dto.FormTemplateCreateDTO;
import com.dazzle.asklepios.service.dto.FormTemplateUpdateDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.FormTemplateResponseVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/setup/form-templates")
public class FormTemplateController {

    private final FormTemplateService service;
    private static final Logger LOG = LoggerFactory.getLogger(FormTemplateController.class);

    public FormTemplateController(FormTemplateService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<FormTemplateResponseVM> create(@RequestBody FormTemplateCreateDTO dto) {
        LOG.info("REST request to create FormTemplate: name='{}', facilityId={}, departmentId={}",
                dto.name(), dto.facilityId(), dto.departmentId());
        LOG.debug("Create FormTemplate payload: descriptionLen={}, formJsonLen={}",
                dto.description() != null ? dto.description().length() : 0,
                dto.formJson() != null ? dto.formJson().length() : 0);
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormTemplateResponseVM> update(@PathVariable Long id, @RequestBody FormTemplateUpdateDTO dto) {
        LOG.info("REST request to update FormTemplate id={}: name='{}', facilityId={}, departmentId={}",
                id, dto.name(), dto.facilityId(), dto.departmentId());
        LOG.debug("Update FormTemplate payload: descriptionLen={}, formJsonLen={}",
                dto.description() != null ? dto.description().length() : 0,
                dto.formJson() != null ? dto.formJson().length() : 0);

        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormTemplateResponseVM> get(@PathVariable Long id) {
        LOG.debug("REST request to get FormTemplate id={}", id);
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<List<FormTemplateResponseVM>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) Long departmentId,
            Pageable pageable
    ) {
        Page<FormTemplateResponseVM> page = service.list(facilityId, departmentId, q, pageable);

        LOG.debug("REST request to list FormTemplates: facilityId={}, departmentId={}, page={}, size={}, sort={}",
                facilityId, departmentId,
                pageable != null ? pageable.getPageNumber() : null,
                pageable != null ? pageable.getPageSize() : null,
                pageable != null ? pageable.getSort() : null);
        
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
         return ResponseEntity.ok().headers(headers).body(page.getContent());

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.info("REST request to delete FormTemplate id={}", id);
        service.delete(id);
        LOG.info("REST delete FormTemplate success: id={}", id);
        return ResponseEntity.noContent().build();
    }
}
