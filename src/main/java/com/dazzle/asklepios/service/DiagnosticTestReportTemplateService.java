package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestReportTemplate;
import com.dazzle.asklepios.domain.ReportTemplate;
import com.dazzle.asklepios.repository.DiagnosticTestReportTemplateRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.ReportTemplateRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.diagnostictest.DiagnosticTestTemplateSaveVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class DiagnosticTestReportTemplateService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestReportTemplateService.class);

    private final DiagnosticTestReportTemplateRepository templateRepo;
    private final DiagnosticTestRepository testRepo;
    private final ReportTemplateRepository reportTemplateRepository;

    public DiagnosticTestReportTemplateService(
            DiagnosticTestReportTemplateRepository templateRepo,
            DiagnosticTestRepository testRepo,
            ReportTemplateRepository reportTemplateRepository
    ) {
        this.templateRepo = templateRepo;
        this.testRepo = testRepo;
        this.reportTemplateRepository = reportTemplateRepository;
    }

    // -------- CREATE template for test (if not exists) --------
    public DiagnosticTestReportTemplate create(DiagnosticTestTemplateSaveVM vm) {
        LOG.debug("Create DiagnosticTestReportTemplate testId={} payload={}", vm.diagnosticTestId(), vm);

        DiagnosticTest test = testRepo.findById(vm.diagnosticTestId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "testNotFound",
                        "diagnosticTestReportTemplate",
                        "Diagnostic test not found."
                ));

        // ممنوع انشاء Template ثانية لنفس test
        if (templateRepo.findByDiagnosticTest_Id(vm.diagnosticTestId()).isPresent()) {
            throw new BadRequestAlertException(
                    "alreadyExists",
                    "diagnosticTestReportTemplate",
                    "Template already exists for this test. Use update."
            );
        }

        DiagnosticTestReportTemplate t = DiagnosticTestReportTemplate.builder()
                .diagnosticTest(test)
                .name(vm.name())
                .templateValue(vm.templateValue())
                .isActive(vm.isActive() != null ? vm.isActive() : true)
                .build();

        return templateRepo.save(t);
    }

    // -------- UPDATE existing template by testId --------
    public DiagnosticTestReportTemplate update(Long testId, DiagnosticTestTemplateSaveVM vm) {
        LOG.debug("Update DiagnosticTestReportTemplate testId={} payload={}", testId, vm);

        return templateRepo.findByDiagnosticTest_Id(testId)
                .map(existing -> {
                    existing.setName(vm.name());
                    existing.setTemplateValue(vm.templateValue());
                    existing.setIsActive(vm.isActive() != null ? vm.isActive() : existing.getIsActive());
                    return templateRepo.save(existing);
                })
                .orElseThrow(() -> new BadRequestAlertException(
                        "notFound",
                        "diagnosticTestReportTemplate",
                        "No template found for this test. Use create."
                ));
    }

    @Transactional(readOnly = true)
    public Optional<DiagnosticTestReportTemplate> findByTestId(Long testId) {
        return templateRepo.findByDiagnosticTest_Id(testId);
    }

    // assignFromLibrary زي ما عندك
    public DiagnosticTestReportTemplate assignFromLibrary(Long testId, Long templateId) {
        DiagnosticTest test = testRepo.findById(testId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "testNotFound",
                        "diagnosticTestReportTemplate",
                        "Diagnostic test not found."
                ));

        ReportTemplate library = reportTemplateRepository.findById(templateId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "templateNotFound",
                        "diagnosticTestReportTemplate",
                        "Report template not found."
                ));

        return templateRepo.findByDiagnosticTest_Id(testId)
                .map(existing -> {
                    existing.setName(library.getName());
                    existing.setTemplateValue(library.getTemplateValue());
                    existing.setIsActive(true);
                    return templateRepo.save(existing);
                })
                .orElseGet(() -> templateRepo.save(
                        DiagnosticTestReportTemplate.builder()
                                .diagnosticTest(test)
                                .name(library.getName())
                                .templateValue(library.getTemplateValue())
                                .isActive(true)
                                .build()
                ));
    }

    public void delete(Long id) {
        templateRepo.deleteById(id);
    }
}
