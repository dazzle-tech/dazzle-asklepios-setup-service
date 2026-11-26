package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DentalChartAgeRule;
import com.dazzle.asklepios.domain.enumeration.DentalChartType;
import com.dazzle.asklepios.domain.enumeration.ToothNumberingSystem;
import com.dazzle.asklepios.repository.DentalChartAgeRuleRepository;
import com.dazzle.asklepios.repository.DentalChartAgeRuleSpecifications;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    /** Mixed charts: up to 16 teeth per arch and 32 in total (primary and permanent counted together). */
    private static final int MAX_TEETH_PER_ARCH = 16;
    private static final int MAX_TEETH_TOTAL = 32;

    /** Upper arch: permanent Tooth1-Tooth16 and primary ToothA-ToothJ; the rest is the lower arch. */
    private static final Set<String> UPPER_ARCH_TEETH = Stream.concat(
                    IntStream.rangeClosed(1, 16).mapToObj(i -> "Tooth" + i),
                    IntStream.rangeClosed('A', 'J').mapToObj(c -> "Tooth" + (char) c))
            .collect(Collectors.toCollection(HashSet::new));

    private static final Set<String> ALL_TEETH = Stream.concat(PERMANENT_TEETH.stream(), PRIMARY_TEETH.stream())
            .collect(Collectors.toCollection(HashSet::new));

    private final DentalChartAgeRuleRepository repository;
    private final DentalToothNumberingMethodService numberingMethodService;

    public DentalChartAgeRuleService(
            DentalChartAgeRuleRepository repository,
            DentalToothNumberingMethodService numberingMethodService
    ) {
        this.repository = repository;
        this.numberingMethodService = numberingMethodService;
    }

    public DentalChartAgeRule create(DentalChartAgeRule incoming) {
        LOG.info("[CREATE] DentalChartAgeRule payload={}", incoming);
        validateFields(incoming, null);
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
        Optional<DentalChartAgeRule> stored = repository.findById(id);
        // An unchanged reference to a since-deactivated method is preserved, never replaced silently.
        validateFields(incoming, stored.map(DentalChartAgeRule::getNumberingMethodId).orElse(null));

        DentalChartAgeRule existing = stored
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
        existing.setNumberingMethodId(incoming.getNumberingMethodId());
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

    /** Optional filters combined with AND, so the setup screen can search several columns at once. */
    @Transactional(readOnly = true)
    public Page<DentalChartAgeRule> findAll(Boolean isActive, String name, DentalChartType chartType, Pageable pageable) {
        return repository.findAll(DentalChartAgeRuleSpecifications.search(isActive, name, chartType), pageable);
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

    private void validateFields(DentalChartAgeRule rule, Long preservedMethodId) {
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
        // A new or changed method must be active; an unchanged stored reference may stay as it is.
        boolean methodUnchanged = rule.getNumberingMethodId() != null
                && rule.getNumberingMethodId().equals(preservedMethodId);
        if (rule.getNumberingMethodId() != null
                && !methodUnchanged
                && !numberingMethodService.isActiveMethod(rule.getNumberingMethodId())) {
            throw new BadRequestAlertException(
                    "Tooth numbering method must exist and be active",
                    ENTITY_NAME,
                    "numberingMethod.invalid"
            );
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
        Set<String> seen = new HashSet<>();
        for (String tooth : toothNumbers) {
            if (!seen.add(tooth)) {
                throw new BadRequestAlertException(
                        "Duplicate tooth identifier: " + tooth,
                        ENTITY_NAME,
                        "toothNumbers.duplicate"
                );
            }
        }
        if (toothNumbers.size() > MAX_TEETH_TOTAL) {
            throw new BadRequestAlertException(
                    "A chart can have at most " + MAX_TEETH_TOTAL + " teeth",
                    ENTITY_NAME,
                    "toothNumbers.maxTotal"
            );
        }
        long upperCount = toothNumbers.stream().filter(UPPER_ARCH_TEETH::contains).count();
        long lowerCount = toothNumbers.stream().filter(t -> ALL_TEETH.contains(t) && !UPPER_ARCH_TEETH.contains(t)).count();
        if (upperCount > MAX_TEETH_PER_ARCH || lowerCount > MAX_TEETH_PER_ARCH) {
            throw new BadRequestAlertException(
                    "Each arch can have at most " + MAX_TEETH_PER_ARCH + " teeth",
                    ENTITY_NAME,
                    "toothNumbers.maxPerArch"
            );
        }
        for (String tooth : toothNumbers) {
            String counterpart = counterpartOf(tooth);
            if (counterpart != null && seen.contains(counterpart)) {
                throw new BadRequestAlertException(
                        "Primary and permanent teeth cannot both occupy one position: " + tooth + " / " + counterpart,
                        ENTITY_NAME,
                        "toothNumbers.positionConflict"
                );
            }
        }
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
     * Primary ToothA-ToothJ sit in the position of permanent Tooth4-Tooth13 (upper) and
     * ToothK-ToothT in the position of Tooth20-Tooth29 (lower). Returns the other tooth
     * of the same position, or null when the tooth has no counterpart.
     */
    private static String counterpartOf(String tooth) {
        if (tooth == null || !ALL_TEETH.contains(tooth)) {
            return null;
        }
        String id = tooth.substring("Tooth".length());
        if (id.length() == 1 && Character.isLetter(id.charAt(0))) {
            int index = id.charAt(0) - 'A';
            return "Tooth" + (index < 10 ? index + 4 : index + 10);
        }
        int number = Integer.parseInt(id);
        if (number >= 4 && number <= 13) {
            return "Tooth" + (char) ('A' + (number - 4));
        }
        if (number >= 20 && number <= 29) {
            return "Tooth" + (char) ('K' + (number - 20));
        }
        return null;
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
     * Resolve the active rule for the patient's age in completed years at the reference date
     * (today when absent), using [ageFrom, ageTo).
     */
    @Transactional(readOnly = true)
    public Optional<DentalChartAgeRule> findActiveRuleForBirthDate(LocalDate birthDate, LocalDate asOfDate) {
        LocalDate reference = asOfDate != null ? asOfDate : LocalDate.now();
        int ageYears = Period.between(birthDate, reference).getYears();
        if (ageYears < 0) {
            return Optional.empty();
        }
        return repository.findByIsActiveTrue().stream()
                .filter(rule -> rule.getAgeFrom() != null && rule.getAgeFrom() <= ageYears)
                .filter(rule -> rule.getAgeTo() == null || ageYears < rule.getAgeTo())
                .findFirst();
    }

    /** Labels of the rule's custom numbering method for a new chart snapshot; empty means legacy numbering. */
    @Transactional(readOnly = true)
    public Optional<Map<String, String>> resolveNumberingLabels(DentalChartAgeRule rule) {
        return numberingMethodService.resolveLabels(rule.getNumberingMethodId());
    }
}
