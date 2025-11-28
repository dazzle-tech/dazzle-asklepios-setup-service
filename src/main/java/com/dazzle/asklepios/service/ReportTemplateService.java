package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ReportTemplate;
import com.dazzle.asklepios.repository.ReportTemplateRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.reporttemplate.ReportTemplateSaveVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ReportTemplateService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportTemplateService.class);

    private final ReportTemplateRepository repository;

    public ReportTemplateService(ReportTemplateRepository repository) {
        this.repository = repository;
    }

    public ReportTemplate save(ReportTemplateSaveVM vm) {
        LOG.debug("Save ReportTemplate payload={}", vm);

        if (vm.id() == null) {
            ReportTemplate t = ReportTemplate.builder()
                    .name(vm.name())
                    .templateValue(vm.templateValue())
                    .isActive(vm.isActive() != null ? vm.isActive() : true)
                    .build();
            return repository.save(t);
        }

        return repository.findById(vm.id())
                .map(existing -> {
                    existing.setName(vm.name());
                    existing.setTemplateValue(vm.templateValue());
                    existing.setIsActive(vm.isActive() != null ? vm.isActive() : existing.getIsActive());
                    return repository.save(existing);
                })
                .orElseThrow(() -> new BadRequestAlertException(
                        "notFound",
                        "reportTemplate",
                        "Template not found."
                ));
    }

    @Transactional(readOnly = true)
    public Page<ReportTemplate> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ReportTemplate> findAllActive(Pageable pageable) {
        return repository.findByIsActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ReportTemplate> findByName(String name, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<ReportTemplate> findOne(Long id) {
        return repository.findById(id);
    }

    public Optional<ReportTemplate> toggleIsActive(Long id) {
        return repository.findById(id)
                .map(t -> {
                    t.setIsActive(!Boolean.TRUE.equals(t.getIsActive()));
                    return repository.save(t);
                });
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
