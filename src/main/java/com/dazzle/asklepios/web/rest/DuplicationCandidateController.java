package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DuplicationCandidate;
import com.dazzle.asklepios.repository.DuplicationCandidateRepository;
import com.dazzle.asklepios.service.DuplicationCandidateService;
import com.dazzle.asklepios.web.rest.vm.DuplicationCandidateCreateVM;
import com.dazzle.asklepios.web.rest.vm.DuplicationCandidateUpdateVM;
import com.dazzle.asklepios.web.rest.vm.DuplicationCandidateResponseVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/duplication-candidates")
@Transactional
public class DuplicationCandidateController {

    private static final Logger LOG = LoggerFactory.getLogger(DuplicationCandidateController.class);

    private final DuplicationCandidateService service;
    private final DuplicationCandidateRepository repository;

    public DuplicationCandidateController(DuplicationCandidateService service,
                                          DuplicationCandidateRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    // ➕ Create
    @PostMapping
    public ResponseEntity<DuplicationCandidateResponseVM> create(
            @RequestBody DuplicationCandidateCreateVM vm,
            Principal principal) {
        LOG.debug("REST request to create DuplicationCandidate : {}", vm);

        DuplicationCandidate entity = new DuplicationCandidate();
        entity.setDob(vm.dob());
        entity.setLastName(vm.lastName());
        entity.setDocumentNo(vm.documentNo());
        entity.setMobileNumber(vm.mobileNumber());
        entity.setGender(vm.gender());

        DuplicationCandidate saved = service.saveRecord(entity, principal != null ? principal.getName() : "system");
        return ResponseEntity.ok(DuplicationCandidateResponseVM.ofEntity(saved));
    }

    // ✏️ Update
    @PutMapping("/{id}")
    public ResponseEntity<DuplicationCandidateResponseVM> update(
            @PathVariable Long id,
            @RequestBody DuplicationCandidateUpdateVM vm) {
        LOG.debug("REST request to update DuplicationCandidate id={} : {}", id, vm);

        Optional<DuplicationCandidate> updated = repository.findById(id).map(existing -> {
            if (vm.dob() != null) existing.setDob(vm.dob());
            if (vm.lastName() != null) existing.setLastName(vm.lastName());
            if (vm.documentNo() != null) existing.setDocumentNo(vm.documentNo());
            if (vm.mobileNumber() != null) existing.setMobileNumber(vm.mobileNumber());
            if (vm.gender() != null) existing.setGender(vm.gender());
            return repository.save(existing);
        });

        return updated
                .map(c -> ResponseEntity.ok(DuplicationCandidateResponseVM.ofEntity(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 📋 Get all
    @GetMapping
    public List<DuplicationCandidateResponseVM> getAll() {
        LOG.debug("REST request to get all DuplicationCandidates");
        return service.getAll().stream()
                .map(DuplicationCandidateResponseVM::ofEntity)
                .collect(Collectors.toList());
    }

    // 🔍 Get one
    @GetMapping("/{id}")
    public ResponseEntity<DuplicationCandidateResponseVM> getOne(@PathVariable Long id) {
        LOG.debug("REST request to get DuplicationCandidate : {}", id);
        return repository.findById(id)
                .map(DuplicationCandidateResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 🗑️ Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST request to delete DuplicationCandidate : {}", id);
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
