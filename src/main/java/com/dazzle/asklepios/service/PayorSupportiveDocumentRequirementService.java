package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.PayorSupportiveDocumentRequirement;
import com.dazzle.asklepios.domain.enumeration.SupportiveDocumentRequirementCode;
import com.dazzle.asklepios.repository.PayorRepository;
import com.dazzle.asklepios.repository.PayorSupportiveDocumentRequirementRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.supportivedocument.PayorSupportiveDocumentRequirementResponseVM;
import com.dazzle.asklepios.web.rest.vm.supportivedocument.PayorSupportiveDocumentRequirementUpdateVM;
import com.dazzle.asklepios.web.rest.vm.supportivedocument.SupportiveDocumentMedicalSheetAvailabilityVM;
import com.dazzle.asklepios.web.rest.vm.supportivedocument.SupportiveDocumentRequirementCatalogVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class PayorSupportiveDocumentRequirementService {

    private static final String ENTITY_NAME = "payorSupportiveDocumentRequirement";

    private static final Logger LOG =
            LoggerFactory.getLogger(PayorSupportiveDocumentRequirementService.class);

    private final PayorRepository payorRepository;
    private final PayorSupportiveDocumentRequirementRepository configurationRepository;
    private final SupportiveDocumentRequirementCatalog catalog;
    private final SupportiveDocumentMedicalSheetAvailabilityCatalog medicalSheetAvailabilityCatalog;

    public PayorSupportiveDocumentRequirementService(
            PayorRepository payorRepository,
            PayorSupportiveDocumentRequirementRepository configurationRepository,
            SupportiveDocumentRequirementCatalog catalog,
            SupportiveDocumentMedicalSheetAvailabilityCatalog medicalSheetAvailabilityCatalog
    ) {
        this.payorRepository = payorRepository;
        this.configurationRepository = configurationRepository;
        this.catalog = catalog;
        this.medicalSheetAvailabilityCatalog = medicalSheetAvailabilityCatalog;
    }

    @Transactional(readOnly = true)
    public List<SupportiveDocumentRequirementCatalogVM> getCatalog() {
        return catalog.listAll();
    }

    @Transactional(readOnly = true)
    public List<SupportiveDocumentMedicalSheetAvailabilityVM> getMedicalSheetAvailability() {
        return medicalSheetAvailabilityCatalog.listAll();
    }

    @Transactional(readOnly = true)
    public PayorSupportiveDocumentRequirementResponseVM getByPayorId(Long payorId) {
        requirePayorExists(payorId);

        return configurationRepository
                .findByPayorId(payorId)
                .map(this::toConfiguredResponse)
                .orElseGet(() -> unconfiguredResponse(payorId));
    }

    public PayorSupportiveDocumentRequirementResponseVM replace(
            Long payorId,
            PayorSupportiveDocumentRequirementUpdateVM vm
    ) {
        requirePayorExists(payorId);

        if (vm == null) {
            throw new BadRequestAlertException(
                    "Request body is required.",
                    ENTITY_NAME,
                    "body.required"
            );
        }

        if (vm.requirementCodes() == null) {
            throw new BadRequestAlertException(
                    "requirementCodes is required.",
                    ENTITY_NAME,
                    "requirementCodes.required"
            );
        }

        Set<SupportiveDocumentRequirementCode> normalized = normalizeAndValidate(vm.requirementCodes());

        PayorSupportiveDocumentRequirement configuration = configurationRepository
                .findByPayorId(payorId)
                .orElseGet(() -> {
                    PayorSupportiveDocumentRequirement created = new PayorSupportiveDocumentRequirement();
                    created.setPayorId(payorId);
                    created.setRequirementCodes(new LinkedHashSet<>());
                    return created;
                });

        configuration.getRequirementCodes().clear();
        configuration.getRequirementCodes().addAll(normalized);

        PayorSupportiveDocumentRequirement saved = configurationRepository.save(configuration);
        LOG.debug(
                "Saved supportive-document configuration payorId={} codeCount={}",
                payorId,
                saved.getRequirementCodes().size()
        );

        return toConfiguredResponse(saved);
    }

    private Set<SupportiveDocumentRequirementCode> normalizeAndValidate(List<String> rawCodes) {
        Set<SupportiveDocumentRequirementCode> normalized = new LinkedHashSet<>();

        for (String raw : rawCodes) {
            if (raw == null || raw.isBlank()) {
                throw new BadRequestAlertException(
                        "Requirement code must not be null or blank.",
                        ENTITY_NAME,
                        "requirementCode.invalid"
                );
            }

            SupportiveDocumentRequirementCode code;
            try {
                code = SupportiveDocumentRequirementCode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestAlertException(
                        "Unknown supportive document requirement code: " + raw,
                        ENTITY_NAME,
                        "requirementCode.unknown"
                );
            }

            if (!catalog.isKnown(code)) {
                throw new BadRequestAlertException(
                        "Unsupported supportive document requirement code: " + code.name(),
                        ENTITY_NAME,
                        "requirementCode.unsupported"
                );
            }

            normalized.add(code);
        }

        return normalized;
    }

    private void requirePayorExists(Long payorId) {
        if (payorId == null) {
            throw new BadRequestAlertException(
                    "Payor id is required.",
                    ENTITY_NAME,
                    "payorId.required"
            );
        }

        if (!payorRepository.existsById(payorId)) {
            throw new NotFoundAlertException(
                    "Payor not found with id " + payorId,
                    "payor",
                    "notfound"
            );
        }
    }

    private PayorSupportiveDocumentRequirementResponseVM unconfiguredResponse(Long payorId) {
        return new PayorSupportiveDocumentRequirementResponseVM(payorId, false, List.of());
    }

    private PayorSupportiveDocumentRequirementResponseVM toConfiguredResponse(
            PayorSupportiveDocumentRequirement configuration
    ) {
        List<String> codes = new ArrayList<>();
        for (SupportiveDocumentRequirementCode catalogCode : SupportiveDocumentRequirementCode.values()) {
            if (configuration.getRequirementCodes().contains(catalogCode)) {
                codes.add(catalogCode.name());
            }
        }

        return new PayorSupportiveDocumentRequirementResponseVM(
                configuration.getPayorId(),
                true,
                List.copyOf(codes)
        );
    }
}
