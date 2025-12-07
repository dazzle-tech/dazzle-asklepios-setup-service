package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.PriceList;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.biling.PriceListTypes;
import com.dazzle.asklepios.repository.PriceListRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListSaveVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
@Transactional
public class PriceListService {

    private static final Logger LOG = LoggerFactory.getLogger(PriceListService.class);

    private final PriceListRepository repo;

    public PriceListService(PriceListRepository repo) {
        this.repo = repo;
    }

    /**
     * Entry point:
     * - if vm.id != null => update a single record
     * - else            => bulk create (requires at least one facility)
     */
    public List<PriceList> save(PriceListSaveVM vm) {
        LOG.debug("Save PriceList payload={}", vm);

        validateDateRange(vm);

        if (vm.id() != null) {
            return update(vm);
        }

        return createBulk(vm);
    }

    // ---------------- UPDATE SINGLE ----------------
    private List<PriceList> update(PriceListSaveVM vm) {

        PriceList existing = repo.findById(vm.id())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notFound",
                        "priceList",
                        "Price list not found."
                ));

        // Facility is optional in update:
        // If facilityIds is provided, take the first one and update facility + currency.
        // If not provided, keep the current facilityId as-is.
        if (vm.facilityIds() != null && !vm.facilityIds().isEmpty()) {
            Long facilityId = vm.facilityIds().get(0);
            if (facilityId == null) {
                throw new BadRequestAlertException(
                        "facilityRequired",
                        "priceList",
                        "Facility id cannot be null."
                );
            }
            existing.setFacilityId(facilityId);
            existing.setCurrency(resolveCurrency(facilityId));
        }

        existing.setName(vm.name());
        existing.setType(vm.type());
        existing.setEffectiveFrom(vm.effectiveFrom());
        existing.setEffectiveTo(vm.effectiveTo());
        existing.setDescription(vm.description());
        existing.setIsActive(vm.isActive() != null ? vm.isActive() : existing.getIsActive());

        return List.of(repo.save(existing));
    }

    // ---------------- BULK CREATE ----------------
    private List<PriceList> createBulk(PriceListSaveVM vm) {

        List<Long> facilityIds = vm.facilityIds();

        // At least one facility must be selected for create.
        if (facilityIds == null || facilityIds.isEmpty()) {
            throw new BadRequestAlertException(
                    "facilityRequired",
                    "priceList",
                    "At least one facility must be selected."
            );
        }

        List<PriceList> created = new ArrayList<>();
        for (Long fid : facilityIds) {
            if (fid == null) {
                throw new BadRequestAlertException(
                        "facilityRequired",
                        "priceList",
                        "Facility id cannot be null."
                );
            }
            PriceList pl = buildEntity(vm, fid);
            created.add(repo.save(pl));
        }

        return created;
    }

    private void validateDateRange(PriceListSaveVM vm) {
        if (vm.effectiveTo() != null && vm.effectiveTo().isBefore(vm.effectiveFrom())) {
            throw new BadRequestAlertException(
                    "dateRangeInvalid",
                    "priceList",
                    "Effective To must be after Effective From."
            );
        }
    }

    private PriceList buildEntity(PriceListSaveVM vm, Long facilityId) {
        return PriceList.builder()
                .facilityId(facilityId)
                .name(vm.name())
                .type(vm.type())
                .currency(resolveCurrency(facilityId))
                .effectiveFrom(vm.effectiveFrom())
                .effectiveTo(vm.effectiveTo())
                .description(vm.description())
                .isActive(vm.isActive() != null ? vm.isActive() : true)
                .build();
    }

    /**
     * Currency logic placeholder:
     * - if facilityId != null => get currency from facility-service
     * - else                 => get default currency (global)
     */
    private Currency resolveCurrency(Long facilityId) {
        return Currency.USD; // temporary
    }

    // ---------------- READ APIs ----------------

    @Transactional(readOnly = true)
    public Page<PriceList> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<PriceList> findAllActive(Pageable pageable) {
        return repo.findByIsActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<PriceList> findByName(String name, Pageable pageable) {
        return repo.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PriceList> findByType(PriceListTypes type, Pageable pageable) {
        return repo.findByType(type, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PriceList> findByTypeAndName(PriceListTypes type, String name, Pageable pageable) {
        return repo.findByTypeAndNameContainingIgnoreCase(type, name, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<PriceList> findOne(Long id) {
        return repo.findById(id);
    }

    public Optional<PriceList> toggleIsActive(Long id) {
        return repo.findById(id)
                .map(pl -> {
                    pl.setIsActive(!Boolean.TRUE.equals(pl.getIsActive()));
                    return repo.save(pl);
                });
    }
}
