package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DentalToothNumberingMethod;
import com.dazzle.asklepios.repository.DentalToothNumberingMethodRepository;
import com.dazzle.asklepios.repository.DentalToothNumberingMethodSpecifications;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@Transactional
public class DentalToothNumberingMethodService {

    private static final String ENTITY_NAME = "dentalToothNumberingMethod";
    private static final Logger LOG = LoggerFactory.getLogger(DentalToothNumberingMethodService.class);

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 255;
    private static final int MAX_LABEL_LENGTH = 12;

    /** Permanent Tooth1-Tooth32 then primary ToothA-ToothT: the full set a method must define to be active. */
    private static final List<String> PERMANENT_TEETH = IntStream.rangeClosed(1, 32)
            .mapToObj(i -> "Tooth" + i)
            .toList();
    private static final List<String> PRIMARY_TEETH = IntStream.rangeClosed('A', 'T')
            .mapToObj(c -> "Tooth" + (char) c)
            .toList();
    private static final List<String> ORDERED_TEETH = Stream.concat(PERMANENT_TEETH.stream(), PRIMARY_TEETH.stream()).toList();
    private static final Set<String> ALL_TEETH = new HashSet<>(ORDERED_TEETH);

    private final DentalToothNumberingMethodRepository repository;

    public DentalToothNumberingMethodService(DentalToothNumberingMethodRepository repository) {
        this.repository = repository;
    }

    public DentalToothNumberingMethod create(DentalToothNumberingMethod incoming) {
        LOG.info("[CREATE] DentalToothNumberingMethod name='{}'", incoming != null ? incoming.getName() : null);
        validate(incoming, null);
        return repository.save(incoming);
    }

    public Optional<DentalToothNumberingMethod> update(Long id, DentalToothNumberingMethod incoming) {
        LOG.info("[UPDATE] DentalToothNumberingMethod id={}", id);
        if (id == null) {
            throw new BadRequestAlertException("Id is required", ENTITY_NAME, "id.required");
        }
        DentalToothNumberingMethod existing = repository.findById(id)
                .orElseThrow(() -> notFound(id));
        validate(incoming, id);

        existing.setName(incoming.getName());
        existing.setDescription(incoming.getDescription());
        existing.setToothLabelMap(incoming.getToothLabelMap());
        existing.setIsActive(incoming.getIsActive());
        return Optional.of(repository.save(existing));
    }

    @Transactional(readOnly = true)
    public Page<DentalToothNumberingMethod> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /** Optional filters combined with AND, so the setup screen can search several columns at once. */
    @Transactional(readOnly = true)
    public Page<DentalToothNumberingMethod> findAll(Boolean isActive, String name, Pageable pageable) {
        return repository.findAll(DentalToothNumberingMethodSpecifications.search(isActive, name), pageable);
    }

    @Transactional(readOnly = true)
    public Optional<DentalToothNumberingMethod> findOne(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<DentalToothNumberingMethod> findByName(String name, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public List<DentalToothNumberingMethod> findActive() {
        return repository.findByIsActiveTrueOrderByNameAsc();
    }

    /** Methods are deactivated, never hard-deleted, because rules and chart snapshots reference them. */
    public Optional<DentalToothNumberingMethod> toggleActive(Long id) {
        LOG.info("[TOGGLE] DentalToothNumberingMethod id={}", id);
        DentalToothNumberingMethod existing = repository.findById(id)
                .orElseThrow(() -> notFound(id));
        boolean activating = !Boolean.TRUE.equals(existing.getIsActive());
        if (activating) {
            validateCompleteForActivation(existing.getToothLabelMap());
        }
        existing.setIsActive(activating);
        return Optional.of(repository.save(existing));
    }

    /**
     * Labels to snapshot onto a new chart: only for an active, complete method.
     * Empty means the caller must fall back to the legacy enum numbering.
     */
    @Transactional(readOnly = true)
    public Optional<Map<String, String>> resolveLabels(Long methodId) {
        if (methodId == null) {
            return Optional.empty();
        }
        return repository.findById(methodId)
                .filter(method -> Boolean.TRUE.equals(method.getIsActive()))
                .map(DentalToothNumberingMethod::getToothLabelMap)
                .filter(labels -> labels.keySet().containsAll(ALL_TEETH));
    }

    @Transactional(readOnly = true)
    public boolean isActiveMethod(Long methodId) {
        return methodId != null && repository.findById(methodId)
                .map(method -> Boolean.TRUE.equals(method.getIsActive()))
                .orElse(false);
    }

    private NotFoundAlertException notFound(Long id) {
        return new NotFoundAlertException(
                "Dental tooth numbering method not found: " + id,
                ENTITY_NAME,
                "notfound"
        );
    }

    private void validate(DentalToothNumberingMethod method, Long excludeId) {
        if (method == null) {
            throw new BadRequestAlertException("Payload is required", ENTITY_NAME, "payload.required");
        }
        String name = method.getName() == null ? "" : method.getName().trim();
        if (name.isEmpty()) {
            throw new BadRequestAlertException("Name is required", ENTITY_NAME, "name.required");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new BadRequestAlertException(
                    "Name must be at most " + MAX_NAME_LENGTH + " characters", ENTITY_NAME, "name.tooLong");
        }
        method.setName(name);
        boolean duplicateName = excludeId == null
                ? repository.existsByNameIgnoreCase(name)
                : repository.existsByNameIgnoreCaseAndIdNot(name, excludeId);
        if (duplicateName) {
            throw new BadRequestAlertException(
                    "A numbering method with this name already exists", ENTITY_NAME, "name.duplicate");
        }
        if (method.getDescription() != null) {
            String description = method.getDescription().trim();
            if (description.length() > MAX_DESCRIPTION_LENGTH) {
                throw new BadRequestAlertException(
                        "Description must be at most " + MAX_DESCRIPTION_LENGTH + " characters",
                        ENTITY_NAME,
                        "description.tooLong");
            }
            method.setDescription(description.isEmpty() ? null : description);
        }
        if (method.getIsActive() == null) {
            method.setIsActive(Boolean.TRUE);
        }

        Map<String, String> cleaned = validateLabels(method.getToothLabelMap());
        method.setToothLabelMap(cleaned);

        // Incomplete or ambiguous definitions may be saved, but only as inactive.
        if (Boolean.TRUE.equals(method.getIsActive())) {
            validateCompleteForActivation(cleaned);
        }
    }

    /** Every provided label must belong to a known tooth and be a trimmed, non-empty, bounded string. */
    private Map<String, String> validateLabels(Map<String, String> labels) {
        Map<String, String> cleaned = new LinkedHashMap<>();
        for (String toothId : ORDERED_TEETH) {
            if (labels.containsKey(toothId)) {
                String raw = labels.get(toothId);
                String label = raw == null ? "" : raw.trim();
                if (label.isEmpty()) {
                    throw new BadRequestAlertException(
                            "Label for " + toothId + " cannot be blank", ENTITY_NAME, "label.blank");
                }
                if (label.length() > MAX_LABEL_LENGTH) {
                    throw new BadRequestAlertException(
                            "Label for " + toothId + " must be at most " + MAX_LABEL_LENGTH + " characters",
                            ENTITY_NAME,
                            "label.tooLong");
                }
                cleaned.put(toothId, label);
            }
        }
        for (String toothId : labels.keySet()) {
            if (!ALL_TEETH.contains(toothId)) {
                throw new BadRequestAlertException(
                        "Unknown tooth identifier: " + toothId, ENTITY_NAME, "label.unknownTooth");
            }
        }
        return cleaned;
    }

    /**
     * Active methods must label all 52 teeth, and no label may repeat inside one dentition.
     * Palmer-style labels carry a quadrant mark in the string itself (for example 8 + corner),
     * so a repeated number in a different quadrant is a different label and stays valid.
     */
    private void validateCompleteForActivation(Map<String, String> labels) {
        for (String toothId : ORDERED_TEETH) {
            if (!labels.containsKey(toothId)) {
                throw new BadRequestAlertException(
                        "Every tooth needs a label before activation (missing " + toothId + ")",
                        ENTITY_NAME,
                        "label.missing");
            }
        }
        assertNoDuplicates(PERMANENT_TEETH, labels, "permanent");
        assertNoDuplicates(PRIMARY_TEETH, labels, "primary");
    }

    private void assertNoDuplicates(List<String> teeth, Map<String, String> labels, String dentition) {
        Set<String> seen = new HashSet<>();
        for (String toothId : teeth) {
            String normalized = labels.get(toothId).toLowerCase(Locale.ROOT);
            if (!seen.add(normalized)) {
                throw new BadRequestAlertException(
                        "Duplicate " + dentition + " label '" + labels.get(toothId) + "' (" + toothId + ")",
                        ENTITY_NAME,
                        "label.duplicate");
            }
        }
    }
}
