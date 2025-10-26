package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.PractitionerDepartment;
import com.dazzle.asklepios.service.PractitionerDepartmentService;
import com.dazzle.asklepios.web.rest.vm.practitionerDepartment.PractitionerDepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.practitionerDepartment.PractitionerDepartmentResponseVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class PractitionerDepartmentController {

    private static final Logger LOG = LoggerFactory.getLogger(PractitionerDepartmentController.class);
    private final PractitionerDepartmentService service;

    public PractitionerDepartmentController(PractitionerDepartmentService service) {
        this.service = service;
    }

    /**
     * {@code POST /practitioner-department} : Create a Practitioner–Department relation.
     */
    @PostMapping("/practitioner-department")
    public ResponseEntity<PractitionerDepartmentResponseVM> create(
            @Valid @RequestBody PractitionerDepartmentCreateVM vm) {

        LOG.debug("REST create Practitioner–Department link payload={}", vm);
        PractitionerDepartment pd = service.create(vm);
        PractitionerDepartmentResponseVM response = PractitionerDepartmentResponseVM.ofEntity(pd);

        return ResponseEntity
                .created(URI.create("/api/setup/practitioner-department/" + pd.getId()))
                .body(response);
    }

    /**
     * {@code GET /practitioner/{id}/departments} : Get all departments of a practitioner.
     */
    @GetMapping("/practitioner/{id}/departments")
    public ResponseEntity<List<PractitionerDepartmentResponseVM>> getByPractitioner(@PathVariable Long id) {
        LOG.debug("REST get Departments for Practitioner {}", id);
        List<PractitionerDepartmentResponseVM> list = service.findByPractitionerId(id)
                .stream().map(PractitionerDepartmentResponseVM::ofEntity).toList();
        return ResponseEntity.ok(list);
    }

    /**
     * {@code DELETE /practitioner/{pId}/departments/{dId}} : Remove a Practitioner–Department link.
     */
    @DeleteMapping("/practitioner/{pId}/departments/{dId}")
    public ResponseEntity<Void> deleteLink(@PathVariable Long pId, @PathVariable Long dId) {
        LOG.debug("REST delete Practitioner–Department link pId={} dId={}", pId, dId);
        service.delete(pId, dId);
        return ResponseEntity.noContent().build();
    }
}
