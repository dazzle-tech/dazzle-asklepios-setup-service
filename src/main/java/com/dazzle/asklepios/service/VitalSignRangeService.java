package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.VitalSignRange;
import com.dazzle.asklepios.domain.enumeration.VitalSignType;
import com.dazzle.asklepios.repository.VitalSignRangeRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class VitalSignRangeService {

    private static final String ENTITY_NAME = "vitalSignRange";
    private static final Logger LOG = LoggerFactory.getLogger(VitalSignRangeService.class);

    private final VitalSignRangeRepository repository;
    private final EntityManager entityManager;

    public VitalSignRangeService(VitalSignRangeRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<VitalSignRange> findAll() {
        return repository.findAllByOrderByVitalSignKeyAscAgeFromMonthsAsc();
    }

    public VitalSignRange create(
            String vitalSignKey,
            Integer ageFromMonths,
            Integer ageToMonths,
            BigDecimal normalMin,
            BigDecimal normalMax
    ) {
        LOG.info("[CREATE] VitalSignRange key={} age=[{}, {}) min={} max={}",
                vitalSignKey, ageFromMonths, ageToMonths, normalMin, normalMax);
        VitalSignType type = VitalSignType.fromKey(vitalSignKey)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Unknown vital sign: " + vitalSignKey, ENTITY_NAME, "vitalSignKey.invalid"));
        validate(ageFromMonths, ageToMonths, normalMin, normalMax);
        lockVitalSign(type.getKey());
        validateNoOverlap(type.getKey(), ageFromMonths, ageToMonths, null);

        return repository.save(VitalSignRange.builder()
                .vitalSignKey(type.getKey())
                .unit(type.getUnit())
                .ageFromMonths(ageFromMonths)
                .ageToMonths(ageToMonths)
                .normalMin(normalMin)
                .normalMax(normalMax)
                .build());
    }

    public VitalSignRange update(
            Long id,
            Integer ageFromMonths,
            Integer ageToMonths,
            BigDecimal normalMin,
            BigDecimal normalMax
    ) {
        LOG.info("[UPDATE] VitalSignRange id={} age=[{}, {}) min={} max={}",
                id, ageFromMonths, ageToMonths, normalMin, normalMax);
        validate(ageFromMonths, ageToMonths, normalMin, normalMax);
        VitalSignRange existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Vital sign range not found: " + id, ENTITY_NAME, "notfound"));

        // Serialise concurrent writers of the same vital sign, then re-read the rule under the lock.
        lockVitalSign(existing.getVitalSignKey());
        validateNoOverlap(existing.getVitalSignKey(), ageFromMonths, ageToMonths, id);

        existing.setAgeFromMonths(ageFromMonths);
        existing.setAgeToMonths(ageToMonths);
        existing.setNormalMin(normalMin);
        existing.setNormalMax(normalMax);
        return repository.save(existing);
    }

    private void validate(Integer ageFrom, Integer ageTo, BigDecimal min, BigDecimal max) {
        if (ageFrom == null || ageFrom < 0) {
            throw new BadRequestAlertException(
                    "Age From must be 0 or more months", ENTITY_NAME, "ageFrom.invalid");
        }
        if (ageTo != null && ageTo <= ageFrom) {
            throw new BadRequestAlertException(
                    "Age To must be greater than Age From", ENTITY_NAME, "ageTo.invalid");
        }
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new BadRequestAlertException(
                    "Normal Min must be less than or equal to Normal Max", ENTITY_NAME, "range.invalid");
        }
    }

    private void validateNoOverlap(String key, Integer ageFrom, Integer ageTo, Long excludeId) {
        boolean overlaps = repository.findByVitalSignKey(key).stream()
                .filter(candidate -> excludeId == null || !excludeId.equals(candidate.getId()))
                .anyMatch(candidate -> rangesOverlap(ageFrom, ageTo, candidate.getAgeFromMonths(), candidate.getAgeToMonths()));
        if (overlaps) {
            throw new BadRequestAlertException(
                    "Age range overlaps an existing range for this vital sign", ENTITY_NAME, "age.overlap");
        }
    }

    /** Half-open intervals [from, to): adjacent ranges do not overlap; a null upper bound is open-ended. */
    private boolean rangesOverlap(Integer from1, Integer to1, Integer from2, Integer to2) {
        int end1 = to1 != null ? to1 : Integer.MAX_VALUE;
        int end2 = to2 != null ? to2 : Integer.MAX_VALUE;
        return from1 < end2 && from2 < end1;
    }

    /**
     * Serialises create/update of the same vital sign with a transaction-scoped PostgreSQL advisory lock, released
     * on commit/rollback. A row lock (@Lock PESSIMISTIC_WRITE, as the financial numbering repositories use) cannot
     * do this: a vital sign with no ranges yet has no row to lock, so two concurrent first creates would both pass
     * the overlap check. Must be called inside the transaction, before the overlap check.
     */
    private void lockVitalSign(String key) {
        entityManager
                .createNativeQuery("select cast(pg_advisory_xact_lock(hashtext(?1)) as text)")
                .setParameter(1, "vital_sign_range:" + key)
                .getResultList();
    }
}
