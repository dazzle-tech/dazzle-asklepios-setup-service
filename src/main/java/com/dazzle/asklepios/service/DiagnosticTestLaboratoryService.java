package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiagnosticTestLaboratory;
import com.dazzle.asklepios.repository.DiagnosticTestLaboratoryRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class DiagnosticTestLaboratoryService {

    private final DiagnosticTestLaboratoryRepository repository;

    public DiagnosticTestLaboratoryService(DiagnosticTestLaboratoryRepository repository) {
        this.repository = repository;
    }

    public DiagnosticTestLaboratory create(DiagnosticTestLaboratory entity) {
        return repository.save(entity);
    }

    public Optional<DiagnosticTestLaboratory> update(Long id, DiagnosticTestLaboratory entity) {
        return repository.findById(id).map(existing -> {
            entity.setId(existing.getId());
            return repository.save(entity);
        });
    }

    @Transactional(readOnly = true)
    public Page<DiagnosticTestLaboratory> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<DiagnosticTestLaboratory> findOne(Long id) {
        return repository.findById(id);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new BadRequestAlertException("DiagnosticTestLaboratory not found", "diagnosticTestLaboratory", "notfound");
        }
        repository.deleteById(id);
    }
}
