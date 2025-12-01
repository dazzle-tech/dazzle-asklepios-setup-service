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
     * create/update:
     * - if vm.id != null => update single record
     * - else => bulk create based on facilityIds
     * <p>
     * facilityIds:
     * - empty/null => create GLOBAL list (facilityId = null)
     * - multiple  => create one list per facilityId
     */
    public List<PriceList> save(PriceListSaveVM vm) {
        LOG.debug("Save PriceList payload={}", vm);

        // validation: date range
        if (vm.effectiveTo() != null && vm.effectiveTo().isBefore(vm.effectiveFrom())) {
            throw new BadRequestAlertException(
                    "dateRangeInvalid",
                    "priceList",
                    "Effective To must be after Effective From."
            );
        }

        // ---------------- UPDATE SINGLE ----------------
        if (vm.id() != null) {
            PriceList existing = repo.findById(vm.id())
                    .orElseThrow(() -> new BadRequestAlertException(
                            "notFound",
                            "priceList",
                            "Price list not found."
                    ));

            Long facilityId = null;
            if (vm.facilityIds() != null && !vm.facilityIds().isEmpty()) {
                // update: منسمح ب facility واحدة فقط
                facilityId = vm.facilityIds().get(0);
            }

            existing.setFacilityId(facilityId);
            existing.setName(vm.name());
            existing.setType(vm.type());
            existing.setCurrency(resolveCurrency(facilityId));
            existing.setEffectiveFrom(vm.effectiveFrom());
            existing.setEffectiveTo(vm.effectiveTo());
            existing.setDescription(vm.description());
            existing.setIsActive(vm.isActive() != null ? vm.isActive() : existing.getIsActive());

            return List.of(repo.save(existing));
        }

        // ---------------- BULK CREATE ----------------
        List<Long> facilityIds = vm.facilityIds();

        // no facility selected => one GLOBAL record
        if (facilityIds == null || facilityIds.isEmpty()) {
            PriceList pl = buildEntity(vm, null);
            return List.of(repo.save(pl));
        }

        // many facilities selected => one record per facilityId
        List<PriceList> created = new ArrayList<>();
        for (Long fid : facilityIds) {
            PriceList pl = buildEntity(vm, fid);
            created.add(repo.save(pl));
        }

        return created;
    }

    private PriceList buildEntity(PriceListSaveVM vm, Long facilityId) {
        return PriceList.builder()
                .facilityId(facilityId) // nullable => global
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
     * currency logic:
     * - if facilityId != null => get currency from facility-service
     * - else => get default currency (global)
     * <p>
     * حالياً placeholder لحد ما تربطي facility-service.
     */
    private Currency resolveCurrency(Long facilityId) {
        // TODO: call facility-service
        // مثال:
        // if (facilityId != null) return facilityClient.getCurrency(facilityId);
        // else return facilityClient.getDefaultCurrency();

        return Currency.USD; // مؤقت
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
