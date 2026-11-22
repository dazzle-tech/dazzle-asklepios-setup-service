package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.PackageSetup;
import com.dazzle.asklepios.domain.PackageSetupItem;
import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.repository.PackageSetupItemRepository;
import com.dazzle.asklepios.repository.PackageSetupRepository;
import com.dazzle.asklepios.repository.PackageSetupSpecifications;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.packageSetup.PackageItemLookupVM;
import com.dazzle.asklepios.web.rest.vm.packageSetup.PackageSetupItemVM;
import com.dazzle.asklepios.web.rest.vm.packageSetup.PackageSetupResponseVM;
import com.dazzle.asklepios.web.rest.vm.packageSetup.PackageSetupSaveVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Packages: header and items are always saved together from the one setup screen. */
@Service
@Transactional
public class PackageSetupService {

    private static final Logger LOG = LoggerFactory.getLogger(PackageSetupService.class);
    private static final String ENTITY = "packageSetup";

    private final PackageSetupRepository packageSetupRepository;
    private final PackageSetupItemRepository packageSetupItemRepository;
    private final PackageItemLookupService packageItemLookupService;

    public PackageSetupService(
            PackageSetupRepository packageSetupRepository,
            PackageSetupItemRepository packageSetupItemRepository,
            PackageItemLookupService packageItemLookupService
    ) {
        this.packageSetupRepository = packageSetupRepository;
        this.packageSetupItemRepository = packageSetupItemRepository;
        this.packageItemLookupService = packageItemLookupService;
    }

    /** Optional filters combined with AND; each package comes with its items. */
    @Transactional(readOnly = true)
    public Page<PackageSetupResponseVM> findAll(
            String code, String name, BigDecimal price, Boolean isActive, Pageable pageable
    ) {
        Page<PackageSetup> page = packageSetupRepository.findAll(
                PackageSetupSpecifications.search(code, name, price, isActive), pageable);

        List<Long> ids = page.getContent().stream().map(PackageSetup::getId).toList();
        Map<Long, List<PackageSetupItem>> itemsByPackage = ids.isEmpty()
                ? Map.of()
                : packageSetupItemRepository.findByPackageSetupIdIn(ids).stream()
                        .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                        .collect(Collectors.groupingBy(PackageSetupItem::getPackageSetupId));

        return page.map(p -> PackageSetupResponseVM.ofEntity(p, itemsByPackage.getOrDefault(p.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public PackageSetupResponseVM findOne(Long id) {
        PackageSetup entity = getOrThrow(id);
        return PackageSetupResponseVM.ofEntity(entity, packageSetupItemRepository.findByPackageSetupIdOrderByIdAsc(id));
    }

    public PackageSetupResponseVM create(PackageSetupSaveVM vm) {
        LOG.info("[CREATE] Package code={} items={}", vm.code(), vm.items() != null ? vm.items().size() : 0);
        String code = vm.code().trim();
        if (packageSetupRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestAlertException("A package with this code already exists", ENTITY, "code.exists");
        }

        PackageSetup entity = PackageSetup.builder()
                .code(code)
                .name(vm.name().trim())
                .description(blankToNull(vm.description()))
                .price(vm.price())
                .isActive(vm.isActive() == null || vm.isActive())
                .build();
        PackageSetup saved = packageSetupRepository.saveAndFlush(entity);

        List<PackageSetupItem> items = saveItems(saved.getId(), vm.items());
        return PackageSetupResponseVM.ofEntity(saved, items);
    }

    public PackageSetupResponseVM update(Long id, PackageSetupSaveVM vm) {
        LOG.info("[UPDATE] Package id={} items={}", id, vm.items() != null ? vm.items().size() : 0);
        PackageSetup entity = getOrThrow(id);
        String code = vm.code().trim();
        if (packageSetupRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new BadRequestAlertException("A package with this code already exists", ENTITY, "code.exists");
        }

        entity.setCode(code);
        entity.setName(vm.name().trim());
        entity.setDescription(blankToNull(vm.description()));
        entity.setPrice(vm.price());
        if (vm.isActive() != null) {
            entity.setIsActive(vm.isActive());
        }
        PackageSetup saved = packageSetupRepository.saveAndFlush(entity);

        // Items are replaced as a whole: the screen always sends the full list.
        packageSetupItemRepository.deleteByPackageSetupId(id);
        List<PackageSetupItem> items = saveItems(id, vm.items());
        return PackageSetupResponseVM.ofEntity(saved, items);
    }

    public PackageSetupResponseVM toggleActive(Long id) {
        PackageSetup entity = getOrThrow(id);
        entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
        PackageSetup saved = packageSetupRepository.saveAndFlush(entity);
        return PackageSetupResponseVM.ofEntity(saved, packageSetupItemRepository.findByPackageSetupIdOrderByIdAsc(id));
    }

    public void delete(Long id) {
        getOrThrow(id);
        // package_setup_item rows go with it (on delete cascade)
        packageSetupItemRepository.deleteByPackageSetupId(id);
        packageSetupRepository.deleteById(id);
        LOG.info("Deleted package id={}", id);
    }

    /** Checks the lines, takes code and name from each item's own setup table, and stores them. */
    private List<PackageSetupItem> saveItems(Long packageSetupId, List<PackageSetupItemVM> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BadRequestAlertException("A package needs at least one item", ENTITY, "items.required");
        }

        Set<String> seen = new HashSet<>();
        Map<PriceListItemType, Set<Long>> idsByType = new EnumMap<>(PriceListItemType.class);
        for (PackageSetupItemVM line : lines) {
            if (line.itemType() == null || line.sourceId() == null) {
                throw new BadRequestAlertException("Each item needs a type and an item", ENTITY, "item.required");
            }
            if (line.quantity() == null || line.quantity() < 1) {
                throw new BadRequestAlertException("Quantity must be at least 1", ENTITY, "item.quantity");
            }
            if (!seen.add(line.itemType() + ":" + line.sourceId())) {
                throw new BadRequestAlertException("The same item is added twice", ENTITY, "item.duplicate");
            }
            idsByType.computeIfAbsent(line.itemType(), t -> new HashSet<>()).add(line.sourceId());
        }

        Map<PriceListItemType, Map<Long, PackageItemLookupVM>> found = new EnumMap<>(PriceListItemType.class);
        idsByType.forEach((type, ids) -> found.put(type, packageItemLookupService.findByIds(type, ids)));

        List<PackageSetupItem> toSave = new ArrayList<>();
        for (PackageSetupItemVM line : lines) {
            PackageItemLookupVM source = found.get(line.itemType()).get(line.sourceId());
            if (source == null) {
                throw new BadRequestAlertException(
                        "Item not found: " + line.itemType() + " id " + line.sourceId(), ENTITY, "item.notfound");
            }
            toSave.add(PackageSetupItem.builder()
                    .packageSetupId(packageSetupId)
                    .itemType(line.itemType())
                    .sourceId(line.sourceId())
                    .itemCode(source.code())
                    .itemName(source.name())
                    .quantity(line.quantity())
                    .build());
        }
        return packageSetupItemRepository.saveAllAndFlush(toSave);
    }

    private PackageSetup getOrThrow(Long id) {
        return packageSetupRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Package not found with id " + id, ENTITY, "notfound"));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
