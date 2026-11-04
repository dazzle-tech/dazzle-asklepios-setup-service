package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DuplicationCandidate;
import com.dazzle.asklepios.domain.enumeration.DuplicationField;
import com.dazzle.asklepios.security.SecurityUtils;
import com.dazzle.asklepios.service.DuplicationCandidateService;
import com.dazzle.asklepios.service.FacilityService;
import com.dazzle.asklepios.web.rest.vm.DuplicationCandidateResponseVM;
import com.dazzle.asklepios.web.rest.vm.facility.FacilityResponseVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/setup/duplication-candidates")
public class DuplicationCandidateController {

    private static final Logger LOG = LoggerFactory.getLogger(DuplicationCandidateController.class);

    private final DuplicationCandidateService service;
    private final FacilityService facilityService;

    public DuplicationCandidateController(
            DuplicationCandidateService service,
            FacilityService facilityService
    ) {
        this.service = service;
        this.facilityService = facilityService;
    }

    /** Create new candidate */
    @PostMapping
    public ResponseEntity<DuplicationCandidateResponseVM> create(
            @RequestBody DuplicationCandidateResponseVM vm
    ) {
        LOG.debug("REST request to create DuplicationCandidate: {}", vm);
        String user = SecurityUtils.getCurrentUserLogin().orElse("system");

        DuplicationCandidate entity = new DuplicationCandidate();
        entity.setFields(vm.fields());
        entity.setIsActive(vm.isActive() != null ? vm.isActive() : true);

        DuplicationCandidate saved = service.create(entity, user);
        return ResponseEntity.ok(DuplicationCandidateResponseVM.ofEntity(saved));
    }

    /** Update existing candidate */
    @PutMapping("/{id}")
    public ResponseEntity<DuplicationCandidateResponseVM> update(
            @PathVariable Long id,
            @RequestBody DuplicationCandidateResponseVM vm
    ) {
        LOG.debug("REST request to update DuplicationCandidate id={}", id);
        String user = SecurityUtils.getCurrentUserLogin().orElse("system");

        DuplicationCandidate updateEntity = new DuplicationCandidate();
        updateEntity.setFields(vm.fields());
        updateEntity.setIsActive(vm.isActive());

        Optional<DuplicationCandidate> updated = service.update(id, updateEntity, user);

        return updated
                .map(DuplicationCandidateResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Get all or filter by role */
    @GetMapping
    public ResponseEntity<List<DuplicationCandidateResponseVM>> findAll(
            @RequestParam(required = false) String role
    ) {
        LOG.debug("REST request to get DuplicationCandidates (filter role={})", role);

        List<DuplicationCandidate> list;
        if (role != null && !role.isBlank()) {
            list = service.findByRoleFilter(role.trim());
        } else {
            list = service.findAll();
        }

        List<DuplicationCandidateResponseVM> result = list.stream()
                .map(DuplicationCandidateResponseVM::ofEntity)
                .toList();

        return ResponseEntity.ok(result);
    }

    /** Filter by rule */
    @GetMapping("/by-rule/{rule}")
    public ResponseEntity<List<DuplicationCandidateResponseVM>> getByRule(@PathVariable String rule) {
        LOG.debug("REST get DuplicationCandidates by rule='{}'", rule);
        return ResponseEntity.ok(
                service.findByRule(rule.trim()).stream()
                        .map(DuplicationCandidateResponseVM::ofEntity)
                        .toList()
        );
    }

    /** Filter by status */
    @GetMapping("/by-status")
    public ResponseEntity<List<DuplicationCandidateResponseVM>> getByStatus(@RequestParam boolean isActive) {
        LOG.debug("REST get DuplicationCandidates by status isActive={}", isActive);
        return ResponseEntity.ok(
                service.findByStatus(isActive).stream()
                        .map(DuplicationCandidateResponseVM::ofEntity)
                        .toList()
        );
    }

    /** Filter by one duplication field value (e.g. field=FIRST_NAME&value=true) */
    @GetMapping("/by-field")
    public ResponseEntity<List<DuplicationCandidateResponseVM>> getByField(
            @RequestParam DuplicationField field,
            @RequestParam boolean value
    ) {
        LOG.debug("REST get DuplicationCandidates by field={} value={}", field, value);
        return ResponseEntity.ok(
                service.findByField(field, value).stream()
                        .map(DuplicationCandidateResponseVM::ofEntity)
                        .toList()
        );
    }

    /** Get one by id */
    @GetMapping("/{id}")
    public ResponseEntity<DuplicationCandidateResponseVM> findOne(@PathVariable Long id) {
        LOG.debug("REST request to get DuplicationCandidate id={}", id);
        return service.findOne(id)
                .map(DuplicationCandidateResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Deactivate */
    @PutMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        LOG.debug("REST request to deactivate DuplicationCandidate id={}", id);
        String user = SecurityUtils.getCurrentUserLogin().orElse("system");
        boolean ok = service.deactivate(id, user);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /** Reactivate */
    @PutMapping("/reactivate/{id}")
    public ResponseEntity<Void> reactivate(@PathVariable Long id) {
        LOG.debug("REST request to reactivate DuplicationCandidate id={}", id);
        String user = SecurityUtils.getCurrentUserLogin().orElse("system");
        boolean ok = service.reactivate(id, user);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /** Delete permanently */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST request to delete DuplicationCandidate id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Get facilities linked/unlinked to a role */
    @GetMapping("/available-for-role/{roleId}")
    public ResponseEntity<List<FacilityResponseVM>> getAvailableForRole(@PathVariable Long roleId) {
        LOG.debug("REST request to get facilities for roleId={}", roleId);
        List<FacilityResponseVM> list = facilityService.findUnlinkedOrLinkedToRule(roleId);
        return ResponseEntity.ok(list);
    }
}
