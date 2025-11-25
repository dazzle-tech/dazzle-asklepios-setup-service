package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestCoding;
import com.dazzle.asklepios.domain.enumeration.MedicalCodeType;
import com.dazzle.asklepios.repository.DiagnosticTestCodingRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DiagnosticTestCodingService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestCodingService.class);

    private final DiagnosticTestCodingRepository repository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final CptCodeService cptCodeService;
    private final CdtCodeService cdtCodeService;
    private final Icd10Service icd10Service;
    private final LoincCodeService loincCodeService;

    public DiagnosticTestCodingService(
            DiagnosticTestCodingRepository repository,
            DiagnosticTestRepository diagnosticTestRepository,
            CptCodeService cptCodeService,
            CdtCodeService cdtCodeService,
            Icd10Service icd10Service,
            LoincCodeService loincCodeService
    ) {
        this.repository = repository;
        this.diagnosticTestRepository = diagnosticTestRepository;
        this.cptCodeService = cptCodeService;
        this.cdtCodeService = cdtCodeService;
        this.icd10Service = icd10Service;
        this.loincCodeService = loincCodeService;
    }

    public DiagnosticTestCoding create(Long diagnosticTestId, DiagnosticTestCoding input) {
        LOG.debug("Request to create DiagnosticTestCoding for diagnosticTestId={} payload={}", diagnosticTestId, input);

        if (diagnosticTestId == null) {
            throw new BadRequestAlertException("DiagnosticTest ID is required", "diagnosticTestCoding", "diagnostictestid.required");
        }
        if (input == null) {
            throw new BadRequestAlertException("DiagnosticTestCoding payload is required", "diagnosticTestCoding", "payload.required");
        }
        if (input.getCodeType() == null) {
            throw new BadRequestAlertException("codeType is required", "diagnosticTestCoding", "codetype.required");
        }
        if (input.getCodeId() == null || input.getCodeId().isBlank()) {
            throw new BadRequestAlertException("codeId is required", "diagnosticTestCoding", "codeid.required");
        }

        DiagnosticTest diagnosticTest = diagnosticTestRepository.findById(diagnosticTestId)
                .orElseThrow(() -> new NotFoundAlertException(
                        "DiagnosticTest not found with id " + diagnosticTestId, "diagnosticTest", "notfound"
                ));

        DiagnosticTestCoding entity = DiagnosticTestCoding.builder()
                .diagnosticTest(diagnosticTest)
                .codeType(input.getCodeType())
                .codeId(input.getCodeId().trim())
                .build();

        try {
            DiagnosticTestCoding saved = repository.saveAndFlush(entity);
            LOG.debug("Created DiagnosticTestCoding: {}", saved);
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            String msg = extractMessage(ex);
            LOG.warn("DB constraint violation while creating DiagnosticTestCoding (diagnosticTestId={}, codeType={}, codeId={}): {}",
                    diagnosticTestId, input.getCodeType(), input.getCodeId(), msg, ex);

            if (isUniqueViolation(msg, "uk_diagnosticcoding_testid_codetype_codeid")) {
                throw new BadRequestAlertException(
                        "A DiagnosticTestCoding with the same (diagnosticTest, codeType, codeId) already exists.",
                        "diagnosticTestCoding",
                        "unique.diagnostictestcoding"
                );
            }
            throw new BadRequestAlertException(
                    "Database constraint violated while creating DiagnosticTestCoding.",
                    "diagnosticTestCoding",
                    "db.constraint"
            );
        }
    }

    @Transactional(readOnly = true)
    public Page<DiagnosticTestCoding> findAll(Pageable pageable) {
        LOG.debug("Request to get DiagnosticTestCoding with pagination: {}", pageable);
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<DiagnosticTestCoding> findByDiagnosticTestId(Long diagnosticTestId, Pageable pageable) {
        LOG.debug("Request to get DiagnosticTestCoding by diagnosticTestId={} pageable={}", diagnosticTestId, pageable);
        if (diagnosticTestId == null) {
            throw new BadRequestAlertException("DiagnosticTest ID is required", "diagnosticTestCoding", "diagnostictestid.required");
        }
        return repository.findByDiagnosticTestId(diagnosticTestId, pageable);
    }

    public void delete(Long id) {
        LOG.debug("Request to delete DiagnosticTestCoding : {}", id);

        if (!repository.existsById(id)) {
            throw new NotFoundAlertException("DiagnosticTestCoding not found with id " + id, "diagnosticTestCoding", "notfound");
        }

        repository.deleteById(id);
        LOG.debug("Deleted DiagnosticTestCoding id={}", id);
    }

    @Transactional(readOnly = true)
    public Page<CodeOptionDTO> findCodesByType(MedicalCodeType type, Pageable pageable) {
        LOG.debug("Request(findCodesByType) type={} pageable={}", type, pageable);

        if (type == null) {
            throw new BadRequestAlertException("MedicalCodeType is required", "diagnosticTestCoding", "codetype.required");
        }

        return switch (type) {
            case CPT_CODES ->
                    cptCodeService.findAll(pageable)
                            .map(c -> new CodeOptionDTO(c.getId(), c.getCode(), c.getDescription()));
            case CDT_CODES ->
                    cdtCodeService.findAll(pageable)
                            .map(c -> new CodeOptionDTO(c.getId(), c.getCode(), c.getDescription()));
            case ICD10_CODES ->
                    icd10Service.findAll(pageable)
                            .map(c -> new CodeOptionDTO(c.getId(), c.getCode(), c.getDescription()));
            case LOINC_CODES ->
                    loincCodeService.findAll(pageable)
                            .map(c -> new CodeOptionDTO(c.getId(), c.getCode(), c.getDescription()));
        };
    }

    private String extractMessage(Throwable ex) {
        Throwable root = getRootCause(ex);
        return (root != null && root.getMessage() != null ? root.getMessage() : ex.getMessage());
    }

    private Throwable getRootCause(Throwable ex) {
        Throwable result = ex;
        while (result.getCause() != null && result.getCause() != result) {
            result = result.getCause();
        }
        return result;
    }

    private boolean isUniqueViolation(String lowerMsg, String expectedConstraintName) {
        if (lowerMsg == null) return false;
        String m = lowerMsg.toLowerCase();
        return m.contains(expectedConstraintName.toLowerCase())
                || m.contains("unique constraint")
                || m.contains("duplicate key")
                || m.contains("duplicate entry");
    }

    public record CodeOptionDTO(Long id, String code, String description) {}
}
