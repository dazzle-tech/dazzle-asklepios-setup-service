package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ApLovValue;
import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.ProcedureCategory;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.repository.ApLovValueRepository;
import com.dazzle.asklepios.repository.BrandMedicationRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.ProcedureRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.springframework.stereotype.Service;

@Service
public class CatalogTargetService {

    private static final String ENTITY = "coverageContract";

    private final CoverageLookupService coverageLookupService;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final ProcedureRepository procedureRepository;
    private final BrandMedicationRepository brandMedicationRepository;
    private final ApLovValueRepository apLovValueRepository;

    public CatalogTargetService(
            CoverageLookupService coverageLookupService,
            DiagnosticTestRepository diagnosticTestRepository,
            ProcedureRepository procedureRepository,
            BrandMedicationRepository brandMedicationRepository,
            ApLovValueRepository apLovValueRepository
    ) {
        this.coverageLookupService = coverageLookupService;
        this.diagnosticTestRepository = diagnosticTestRepository;
        this.procedureRepository = procedureRepository;
        this.brandMedicationRepository = brandMedicationRepository;
        this.apLovValueRepository = apLovValueRepository;
    }

    public void requireItem(BillingItemTypes source, Long sourceId) {
        if (source == null) {
            throw new BadRequestAlertException("Category is required.", ENTITY, "sourceRequired");
        }
        if (sourceId == null) {
            throw new BadRequestAlertException("Item is required.", ENTITY, "sourceIdRequired");
        }
        switch (source) {
            case SERVICE -> requireActive(coverageLookupService.requireActiveService(sourceId).getIsActive(), "Service");
            case PROCEDURE -> requireActive(requireProcedure(sourceId).getIsActive(), "Procedure");
            case LABORATORY, RADIOLOGY, PATHOLOGY -> requireDiagnostic(source, sourceId);
            case MEDICATION -> requireActive(requireMedication(sourceId).getIsActive(), "Medication");
            default -> throw new BadRequestAlertException("Category is not supported.", ENTITY, "sourceInvalid");
        }
    }

    public void requireSubcategory(BillingItemTypes source, String subcategoryKey) {
        if (source == null) {
            throw new BadRequestAlertException("Category is required.", ENTITY, "sourceRequired");
        }
        if (subcategoryKey == null || subcategoryKey.isBlank()) {
            throw new BadRequestAlertException("Subcategory is required.", ENTITY, "subcategoryRequired");
        }
        String key = subcategoryKey.trim();
        switch (source) {
            case LABORATORY -> requireLov(key, "LAB_CATEGORIES");
            case RADIOLOGY -> requireLov(key, "RAD_CATEGORIES");
            case PATHOLOGY -> requireLov(key, "PATH_CATEGORY");
            case PROCEDURE -> requireEnum(key, ProcedureCategory::valueOf, "Procedure subcategory");
            case SERVICE -> requireEnum(key, ServiceCategory::valueOf, "Service subcategory");
            default -> throw new BadRequestAlertException(
                    "This category has no subcategory.",
                    ENTITY,
                    "subcategoryUnsupported"
            );
        }
    }

    public String itemLabel(BillingItemTypes source, Long sourceId) {
        if (source == null || sourceId == null) {
            return null;
        }
        return switch (source) {
            case SERVICE -> {
                ServiceSetup service = coverageLookupService.findService(sourceId);
                yield service == null ? null : service.getName();
            }
            case PROCEDURE -> procedureRepository.findById(sourceId)
                    .map(Procedure::getName)
                    .orElse(null);
            case LABORATORY, RADIOLOGY, PATHOLOGY -> diagnosticTestRepository.findById(sourceId)
                    .map(DiagnosticTest::getName)
                    .orElse(null);
            case MEDICATION -> brandMedicationRepository.findById(sourceId)
                    .map(BrandMedication::getName)
                    .orElse(null);
            default -> null;
        };
    }

    public String subcategoryLabel(BillingItemTypes source, String subcategoryKey) {
        if (source == null || subcategoryKey == null || subcategoryKey.isBlank()) {
            return null;
        }
        String key = subcategoryKey.trim();
        if (source == BillingItemTypes.LABORATORY
                || source == BillingItemTypes.RADIOLOGY
                || source == BillingItemTypes.PATHOLOGY) {
            return apLovValueRepository.findByKey(key)
                    .map(ApLovValue::getLovDisplayVale)
                    .filter(value -> value != null && !value.isBlank())
                    .orElse(key);
        }
        return enumLabel(key);
    }

    private void requireDiagnostic(BillingItemTypes source, Long sourceId) {
        DiagnosticTest test = diagnosticTestRepository.findById(sourceId)
                .orElseThrow(() -> new BadRequestAlertException("Item was not found.", ENTITY, "sourceNotFound"));
        if (test.getType() == null || !test.getType().name().equals(source.name())) {
            throw new BadRequestAlertException("Item does not belong to this category.", ENTITY, "sourceMismatch");
        }
        requireActive(test.getIsActive(), "Item");
    }

    private Procedure requireProcedure(Long sourceId) {
        return procedureRepository.findById(sourceId)
                .orElseThrow(() -> new BadRequestAlertException("Procedure was not found.", ENTITY, "sourceNotFound"));
    }

    private BrandMedication requireMedication(Long sourceId) {
        return brandMedicationRepository.findById(sourceId)
                .orElseThrow(() -> new BadRequestAlertException("Medication was not found.", ENTITY, "sourceNotFound"));
    }

    private void requireLov(String key, String lovCode) {
        ApLovValue value = apLovValueRepository.findByKey(key)
                .orElseThrow(() -> new BadRequestAlertException("Subcategory was not found.", ENTITY, "subcategoryNotFound"));
        if (value.getLovCode() == null || !lovCode.equalsIgnoreCase(value.getLovCode().trim())) {
            throw new BadRequestAlertException("Subcategory does not belong to this category.", ENTITY, "subcategoryMismatch");
        }
    }

    private void requireEnum(String key, java.util.function.Function<String, ?> parser, String label) {
        try {
            parser.apply(key);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestAlertException(label + " was not found.", ENTITY, "subcategoryNotFound");
        }
    }

    private void requireActive(Boolean active, String label) {
        if (Boolean.FALSE.equals(active)) {
            throw new BadRequestAlertException(label + " is inactive.", ENTITY, "sourceInactive");
        }
    }

    private String enumLabel(String key) {
        String[] words = key.trim().toLowerCase().split("_");
        StringBuilder label = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (!label.isEmpty()) {
                label.append(' ');
            }
            label.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return label.toString();
    }
}
