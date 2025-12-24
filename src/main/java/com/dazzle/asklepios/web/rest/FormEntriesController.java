package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.FormEntriesService;
import com.dazzle.asklepios.service.dto.FormEntryCreateDTO;
import com.dazzle.asklepios.service.dto.FormEntryUpdateDTO;
import com.dazzle.asklepios.web.rest.vm.FormEntryResponseVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import java.util.List;

@RestController
@RequestMapping("/api/setup/form-entries")
public class FormEntriesController {

    private final FormEntriesService service;

    public FormEntriesController(FormEntriesService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<FormEntryResponseVM> create(@RequestBody FormEntryCreateDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormEntryResponseVM> update(@PathVariable Long id, @RequestBody FormEntryUpdateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormEntryResponseVM> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<List<FormEntryResponseVM>> list(
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        Page<FormEntryResponseVM> page = service.list(facilityId, departmentId, templateId, q, pageable);

        // if you use PaginationUtil headers in your project, plug it here
        return ResponseEntity.ok(page.getContent());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
