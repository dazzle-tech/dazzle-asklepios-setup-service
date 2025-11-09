package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.ProcedureCoding;
import com.dazzle.asklepios.domain.enumeration.MedicalCodeType;
import com.dazzle.asklepios.repository.ProcedureCodingRepository;
import com.dazzle.asklepios.repository.ProcedureRepository;
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
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class ProcedureCodingService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcedureCodingService.class);
    private final ProcedureCodingRepository repository;
    private final ProcedureRepository procedureRepository;
    private final CptCodeService cptCodeService;
    private final CdtCodeService cdtCodeService;
    private final Icd10Service icd10Service;
    private final LoincCodeService loincCodeService;

    public ProcedureCodingService(
            ProcedureCodingRepository repository,
            ProcedureRepository procedureRepository,
            CptCodeService cptCodeService,
            CdtCodeService cdtCodeService,
            Icd10Service icd10Service,
            LoincCodeService loincCodeService
    ) {
        this.repository = repository;
        this.procedureRepository = procedureRepository;
        this.cptCodeService = cptCodeService;
        this.cdtCodeService = cdtCodeService;
        this.icd10Service = icd10Service;
        this.loincCodeService = loincCodeService;
    }

    // ====================== CREATE ======================
    public ProcedureCoding create(Long procedureId, ProcedureCoding input) {
        LOG.debug("Request to create ProcedureCoding for procedureId={} payload={}", procedureId, input);

        if (procedureId == null) {
            throw new BadRequestAlertException("Procedure ID is required", "procedureCoding", "procedureid.required");
        }
        if (input == null) {
            throw new BadRequestAlertException("ProcedureCoding payload is required", "procedureCoding", "payload.required");
        }
        if (input.getCodeType() == null) {
            throw new BadRequestAlertException("codeType is required", "procedureCoding", "codetype.required");
        }
        if (input.getCodeId() == null || input.getCodeId().isBlank()) {
            throw new BadRequestAlertException("codeId is required", "procedureCoding", "codeid.required");
        }

        Procedure procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Procedure not found with id " + procedureId, "procedure", "notfound"
                ));

        ProcedureCoding entity = ProcedureCoding.builder()
                .procedure(procedure)
                .codeType(input.getCodeType())
                .codeId(input.getCodeId().trim())
                .build();

        try {
            ProcedureCoding saved = repository.saveAndFlush(entity);
            LOG.debug("Created ProcedureCoding: {}", saved);
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            String msg = extractMessage(ex);
            LOG.warn("DB constraint violation while creating ProcedureCoding (procedureId={}, codeType={}, codeId={}): {}",
                    procedureId, input.getCodeType(), input.getCodeId(), msg, ex);

            if (isUniqueViolation(msg, "uk_procedurecoding_procedureid_codetype_codeid")) {
                throw new BadRequestAlertException(
                        "A ProcedureCoding with the same (procedure, codeType, codeId) already exists.",
                        "procedureCoding",
                        "unique.procedurecoding"
                );
            }
            throw new BadRequestAlertException(
                    "Database constraint violated while creating ProcedureCoding.",
                    "procedureCoding",
                    "db.constraint"
            );
        }
    }

    @Transactional(readOnly = true)
    public Page<ProcedureCoding> findAll(Pageable pageable) {
        LOG.debug("Request to get ProcedureCoding with pagination: {}", pageable);
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProcedureCoding> findByProcedureId(Long procedureId, Pageable pageable) {
        LOG.debug("Request to get ProcedureCoding by procedureId={} pageable={}", procedureId, pageable);
        if (procedureId == null) {
            throw new BadRequestAlertException("Procedure ID is required", "procedureCoding", "procedureid.required");
        }
        return repository.findByProcedureId(procedureId, pageable);
    }

    public void delete(Long id) {
        LOG.debug("Request to delete ProcedureCoding : {}", id);

        if (!repository.existsById(id)) {
            throw new NotFoundAlertException("ProcedureCoding not found with id " + id, "procedureCoding", "notfound");
        }

        repository.deleteById(id);
        LOG.debug("Deleted ProcedureCoding id={}", id);
    }

    @Transactional(readOnly = true)
    public Page<CodeOptionDTO> findCodesByType(MedicalCodeType type, Pageable pageable) {
        LOG.debug("Request(findCodesByType) type={} pageable={}", type, pageable);

        if (type == null) {
            throw new BadRequestAlertException("MedicalCodeType is required", "procedureCoding", "codetype.required");
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

    // ====================== Helpers ======================
    private String extractMessage(Throwable ex) {
        Throwable root = getRootCause(ex);
        return (root != null && root.getMessage() != null ? root.getMessage() : ex.getMessage());
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
