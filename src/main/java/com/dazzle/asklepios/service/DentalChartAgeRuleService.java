package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DentalChartAgeRule;
import com.dazzle.asklepios.domain.enumeration.DentalChartType;
import com.dazzle.asklepios.domain.enumeration.ToothNumberingSystem;
import com.dazzle.asklepios.repository.DentalChartAgeRuleRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@Transactional
public class DentalChartAgeRuleService {

    private static final String ENTITY_NAME = "dentalChartAgeRule";
    private static final Logger LOG = LoggerFactory.getLogger(DentalChartAgeRuleService.class);

    private static final List<String> PERMANENT_TEETH = IntStream.rangeClosed(1, 32)
            .mapToObj(i -> "Tooth" + i)
            .toList();

    private static final List<String> PRIMARY_TEETH = IntStream.rangeClosed('A', 'T')
            .mapToObj(c -> "Tooth" + (char) c)
            .toList();

    private static final Set<String> ALL_TEETH = Stream.concat(PERMANENT_TEETH.stream(), PRIMARY_TEETH.stream())
            .collect(Collectors.toCollection(HashSet::new));

    private final DentalChartAgeRuleRepository repository;

    public DentalChartAgeRuleService(DentalChartAgeRuleRepository repository) {
        this.repository = repository;
    }

    public DentalChartAgeRule create(DentalChartAgeRule incoming) {
        LOG.info("[CREATE] DentalChartAgeRule payload={}", incoming);
        validateFields(incoming);
        if (Boolean.TRUE.equals(incoming.getIsActive())) {
            validateNoOverlappingActiveRanges(null, incoming.getAgeFrom(), incoming.getAgeTo());
        }
        DentalChartAgeRule saved = repository.save(incoming);
        LOG.info("Created DentalChartAgeRule id={} name='{}'", saved.getId(), saved.getName());
        return saved;
    }

    public Optional<DentalChartAgeRule> update(Long id, DentalChartAgeRule incoming) {
        LOG.info("[UPDATE] DentalChartAgeRule id={} payload={}", id, incoming);
        if (id == null) {
            throw new BadRequestAlertException("Id is required", ENTITY_NAME, "id.required");
        }
        validateFields(incoming);

        DentalChartAgeRule existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Dental chart age rule not found: " + id,
                        ENTITY_NAME,
                        "notfound"
                ));

        if (Boolean.TRUE.equals(incoming.getIsActive())) {
            validateNoOverlappingActiveRanges(id, incoming.getAgeFrom(), incoming.getAgeTo());
        }

        existing.setName(incoming.getName());
        existing.setAgeFrom(incoming.getAgeFrom());
        existing.setAgeTo(incoming.getAgeTo());
        existing.setChartType(incoming.getChartType());
        existing.setNumberingSystem(incoming.getNumberingSystem());
        existing.setToothNumberList(incoming.getToothNumberList());
        existing.setIsActive(incoming.getIsActive());

        DentalChartAgeRule saved = repository.save(existing);
        LOG.info("Updated DentalChartAgeRule id={} name='{}'", saved.getId(), saved.getName());
        return Optional.of(saved);
    }

    @Transactional(readOnly = true)
    public Page<DentalChartAgeRule> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<DentalChartAgeRule> findOne(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<DentalChartAgeRule> findByName(String name, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Page<DentalChartAgeRule> findByChartType(DentalChartType chartType, Pageable pageable) {
        return repository.findByChartType(chartType, pageable);
    }

    public Optional<DentalChartAgeRule> toggleActive(Long id) {
        LOG.info("[TOGGLE] DentalChartAgeRule id={}", id);
        DentalChartAgeRule existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Dental chart age rule not found: " + id,
                        ENTITY_NAME,
                        "notfound"
                ));

        boolean activating = !Boolean.TRUE.equals(existing.getIsActive());
        if (activating) {
            validateNoOverlappingActiveRanges(id, existing.getAgeFrom(), existing.getAgeTo());
        }

        existing.setIsActive(activating);
        DentalChartAgeRule saved = repository.save(existing);
        LOG.info("DentalChartAgeRule id={} isActive={}", saved.getId(), saved.getIsActive());
        return Optional.of(saved);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void validateFields(DentalChartAgeRule rule) {
        if (rule == null) {
            throw new BadRequestAlertException("Payload is required", ENTITY_NAME, "payload.required");
        }
        if (rule.getName() == null || rule.getName().isBlank()) {
            throw new BadRequestAlertException("Name is required", ENTITY_NAME, "name.required");
        }
        if (rule.getAgeFrom() == null) {
            throw new BadRequestAlertException("Age From is required", ENTITY_NAME, "ageFrom.required");
        }
        if (rule.getAgeFrom() < 0) {
            throw new BadRequestAlertException("Age From must be nonnegative", ENTITY_NAME, "ageFrom.invalid");
        }
        if (rule.getAgeTo() != null && rule.getAgeTo() < 0) {
            throw new BadRequestAlertException("Age To must be nonnegative", ENTITY_NAME, "ageTo.invalid");
        }
        if (rule.getAgeTo() != null && rule.getAgeFrom() >= rule.getAgeTo()) {
            throw new BadRequestAlertException(
                    "Age From must be less than Age To",
                    ENTITY_NAME,
                    "ageRange.invalid"
            );
        }
        if (rule.getChartType() == null) {
            throw new BadRequestAlertException(
                    "Chart Type must be ADULT, CHILD, or MIXED",
                    ENTITY_NAME,
                    "chartType.required"
            );
        }
        if (rule.getNumberingSystem() == null) {
            rule.setNumberingSystem(ToothNumberingSystem.UNIVERSAL);
        }
        List<String> toothNumbers = rule.getToothNumberList();
        if (toothNumbers.isEmpty()) {
            throw new BadRequestAlertException(
                    "At least one tooth is required",
                    ENTITY_NAME,
                    "toothNumbers.required"
            );
        }
        Set<String> allowed = allowedTeethFor(rule.getChartType());
        for (String tooth : toothNumbers) {
            if (!ALL_TEETH.contains(tooth)) {
                throw new BadRequestAlertException(
                        "Unknown tooth identifier: " + tooth,
                        ENTITY_NAME,
                        "toothNumbers.invalid"
                );
            }
            if (!allowed.contains(tooth)) {
                throw new BadRequestAlertException(
                        "Tooth " + tooth + " is not valid for chart type " + rule.getChartType(),
                        ENTITY_NAME,
                        "toothNumbers.chartTypeMismatch"
                );
            }
        }
        if (rule.getIsActive() == null) {
            rule.setIsActive(Boolean.TRUE);
        }
    }

    /**
     * Defaults used by resolve helpers / docs. MIXED defaults to primary A–T only;
     * permanent teeth are added explicitly in Setup.
     */
    public static List<String> defaultTeethFor(DentalChartType chartType) {
        if (chartType == DentalChartType.CHILD || chartType == DentalChartType.MIXED) {
            return PRIMARY_TEETH;
        }
        return PERMANENT_TEETH;
    }

    private static Set<String> allowedTeethFor(DentalChartType chartType) {
        if (chartType == DentalChartType.CHILD) {
            return new HashSet<>(PRIMARY_TEETH);
        }
        if (chartType == DentalChartType.MIXED) {
            return ALL_TEETH;
        }
        return new HashSet<>(PERMANENT_TEETH);
    }

    /**
     * Active ranges use [ageFrom, ageTo) with null ageTo meaning open-ended.
     */
    private void validateNoOverlappingActiveRanges(Long excludeId, Integer ageFrom, Integer ageTo) {
        List<DentalChartAgeRule> activeRules = repository.findByIsActiveTrue();
        boolean overlaps = activeRules.stream()
                .filter(candidate -> excludeId == null || !excludeId.equals(candidate.getId()))
                .anyMatch(candidate -> rangesOverlap(ageFrom, ageTo, candidate.getAgeFrom(), candidate.getAgeTo()));

        if (overlaps) {
            throw new BadRequestAlertException(
                    "An active age range overlaps this configuration",
                    ENTITY_NAME,
                    "ageRange.overlap"
            );
        }
    }

    private boolean rangesOverlap(Integer from1, Integer to1, Integer from2, Integer to2) {
        int end1 = to1 != null ? to1 : Integer.MAX_VALUE;
        int end2 = to2 != null ? to2 : Integer.MAX_VALUE;
        return from1 < end2 && from2 < end1;
    }

    /**
     * Resolve the active rule for a completed-year age using [ageFrom, ageTo).
     */
    @Transactional(readOnly = true)
    public Optional<DentalChartAgeRule> findActiveRuleForAge(int ageYears) {
        if (ageYears < 0) {
            return Optional.empty();
        }
        return repository.findByIsActiveTrue().stream()
                .filter(rule -> rule.getAgeFrom() != null && rule.getAgeFrom() <= ageYears)
                .filter(rule -> rule.getAgeTo() == null || ageYears < rule.getAgeTo())
                .findFirst();
    }
}
