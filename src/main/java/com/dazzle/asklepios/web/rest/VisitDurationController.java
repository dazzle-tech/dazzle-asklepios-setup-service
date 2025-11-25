package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.VisitDuration;
import com.dazzle.asklepios.domain.enumeration.VisitType;
import com.dazzle.asklepios.service.VisitDurationService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.visitDuration.VisitDurationCreateVM;
import com.dazzle.asklepios.web.rest.vm.visitDuration.VisitDurationResponseVM;
import com.dazzle.asklepios.web.rest.vm.visitDuration.VisitDurationUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class VisitDurationController {

    private static final Logger LOG = LoggerFactory.getLogger(VisitDurationController.class);

    private final VisitDurationService visitDurationService;

    public VisitDurationController(VisitDurationService visitDurationService) {
        this.visitDurationService = visitDurationService;
    }

    @PostMapping("/visit-duration")
    public ResponseEntity<VisitDurationResponseVM> createVisitDuration(
            @Valid @RequestBody VisitDurationCreateVM vm
    ) {
        LOG.debug("REST create VisitDuration payload={}", vm);

        VisitDuration toCreate = VisitDuration.builder()
                .visitType(vm.visitType())
                .durationInMinutes(vm.durationInMinutes())
                .resourceSpecific(Boolean.TRUE.equals(vm.resourceSpecific()))
                .build();

        VisitDuration created = visitDurationService.create(toCreate);
        VisitDurationResponseVM body = VisitDurationResponseVM.ofEntity(created);

        return ResponseEntity
                .created(URI.create("/api/setup/visit-duration/" + created.getId()))
                .body(body);
    }

    @PutMapping("/visit-duration/{id}")
    public ResponseEntity<VisitDurationResponseVM> updateVisitDuration(
            @PathVariable Long id,
            @Valid @RequestBody VisitDurationUpdateVM vm
    ) {
        LOG.debug("REST update VisitDuration id={} payload={}", id, vm);

        VisitDuration patch = new VisitDuration();
        patch.setVisitType(vm.visitType());
        patch.setDurationInMinutes(vm.durationInMinutes());
        patch.setResourceSpecific(vm.resourceSpecific());

        return visitDurationService.update(id, patch)
                .map(VisitDurationResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/visit-duration")
    public ResponseEntity<List<VisitDurationResponseVM>> getAllVisitDurations(
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list VisitDurations pageable={}", pageable);
        Page<VisitDuration> page = visitDurationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        List<VisitDurationResponseVM> body = page.getContent()
                .stream()
                .map(VisitDurationResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/visit-duration/by-type/{visitType}")
    public ResponseEntity<List<VisitDurationResponseVM>> getByVisitType(
            @PathVariable VisitType visitType,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list VisitDurations by visitType={} pageable={}", visitType, pageable);
        Page<VisitDuration> page = visitDurationService.findByVisitType(visitType, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        List<VisitDurationResponseVM> body = page.getContent()
                .stream()
                .map(VisitDurationResponseVM::ofEntity)
                .toList();

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @DeleteMapping("/visit-duration/{id}")
    public ResponseEntity<Void> deleteVisitDuration(@PathVariable Long id) {
        LOG.debug("REST delete VisitDuration id={}", id);
        boolean deleted = visitDurationService.delete(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
