package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Discount;
import com.dazzle.asklepios.domain.PackageSetup;
import com.dazzle.asklepios.domain.PackageSetupDepartment;
import com.dazzle.asklepios.domain.PackageSetupItem;
import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.repository.DiscountRepository;
import com.dazzle.asklepios.repository.PackageSetupDepartmentRepository;
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
import java.util.Objects;
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
    private final PackageSetupDepartmentRepository packageSetupDepartmentRepository;
    private final DiscountRepository discountRepository;

    public PackageSetupService(
            PackageSetupRepository packageSetupRepository,
            PackageSetupItemRepository packageSetupItemRepository,
            PackageItemLookupService packageItemLookupService,
            PackageSetupDepartmentRepository packageSetupDepartmentRepository,
            DiscountRepository discountRepository
    ) {
        this.packageSetupRepository = packageSetupRepository;
        this.packageSetupItemRepository = packageSetupItemRepository;
        this.packageItemLookupService = packageItemLookupService;
        this.packageSetupDepartmentRepository = packageSetupDepartmentRepository;
        this.discountRepository = discountRepository;
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

        Map<Long, List<Long>> departmentsByPackage = departmentsOf(ids);
        Map<Long, Discount> discounts = discountsOf(page.getContent());

        return page.map(p -> PackageSetupResponseVM.ofEntity(
                p,
                itemsByPackage.getOrDefault(p.getId(), List.of()),
                departmentsByPackage.getOrDefault(p.getId(), List.of()),
                p.getDiscountId() == null ? null : discounts.get(p.getDiscountId())));
    }

    @Transactional(readOnly = true)
    public PackageSetupResponseVM findOne(Long id) {
        return response(getOrThrow(id));
    }

    private PackageSetupResponseVM response(PackageSetup entity) {
        return PackageSetupResponseVM.ofEntity(
                entity,
                packageSetupItemRepository.findByPackageSetupIdOrderByIdAsc(entity.getId()),
                departmentsOf(List.of(entity.getId())).getOrDefault(entity.getId(), List.of()),
                entity.getDiscountId() == null
                        ? null
                        : discountRepository.findById(entity.getDiscountId()).orElse(null));
    }

    private Map<Long, List<Long>> departmentsOf(List<Long> packageIds) {
        if (packageIds.isEmpty()) {
            return Map.of();
        }
        return packageSetupDepartmentRepository.findByPackageSetupIdIn(packageIds).stream()
                .collect(Collectors.groupingBy(
                        PackageSetupDepartment::getPackageSetupId,
                        Collectors.mapping(PackageSetupDepartment::getDepartmentId, Collectors.toList())));
    }

    private Long normalizeDiscountId(Long discountId) {
        if (discountId == null) {
            return null;
        }
        if (!discountRepository.existsById(discountId)) {
            throw new BadRequestAlertException("Selected discount was not found", ENTITY, "discount.notfound");
        }
        return discountId;
    }

    private Map<Long, Discount> discountsOf(List<PackageSetup> packages) {
        List<Long> discountIds = packages.stream()
                .map(PackageSetup::getDiscountId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (discountIds.isEmpty()) {
            return Map.of();
        }
        return discountRepository.findAllById(discountIds).stream()
                .collect(Collectors.toMap(Discount::getId, discount -> discount));
    }

    /**
     * Dates in order, at least one department when not for all, and exactly one way of pricing the package:
     * a fixed price, a discount for the whole package, or discounts linked to its items.
     */
    private static void validateHeader(PackageSetupSaveVM vm) {
        if (vm.validFrom() != null && vm.validTo() != null && vm.validTo().isBefore(vm.validFrom())) {
            throw new BadRequestAlertException("Valid To must be on or after Valid From", ENTITY, "validDates");
        }
        if (Boolean.FALSE.equals(vm.allDepartments()) && (vm.departmentIds() == null || vm.departmentIds().isEmpty())) {
            throw new BadRequestAlertException("Choose at least one department, or All Departments", ENTITY, "departments.required");
        }
        boolean fixedPrice = vm.price() != null;
        boolean packageDiscount = vm.discountId() != null;
        boolean itemDiscounts = vm.items() != null && vm.items().stream().anyMatch(i -> i.discountId() != null);
        int options = (fixedPrice ? 1 : 0) + (packageDiscount ? 1 : 0) + (itemDiscounts ? 1 : 0);
        if (options == 0) {
            throw new BadRequestAlertException(
                    "Choose how the package is priced: a fixed price, a discount for the whole package, or discounts on its items",
                    ENTITY, "pricing.required");
        }
        if (options > 1) {
            throw new BadRequestAlertException(
                    "Choose only one: a fixed price, a discount for the whole package, or discounts on its items",
                    ENTITY, "pricing.onlyOne");
        }
    }

    private void saveDepartments(Long packageSetupId, PackageSetupSaveVM vm) {
        packageSetupDepartmentRepository.deleteByPackageSetupId(packageSetupId);
        if (Boolean.FALSE.equals(vm.allDepartments()) && vm.departmentIds() != null) {
            packageSetupDepartmentRepository.saveAll(vm.departmentIds().stream().distinct()
                    .map(d -> PackageSetupDepartment.builder().packageSetupId(packageSetupId).departmentId(d).build())
                    .toList());
        }
    }

    public PackageSetupResponseVM create(PackageSetupSaveVM vm) {
        LOG.info("[CREATE] Package code={} items={}", vm.code(), vm.items() != null ? vm.items().size() : 0);
        String code = vm.code().trim();
        if (packageSetupRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestAlertException("A package with this code already exists", ENTITY, "code.exists");
        }
        validateHeader(vm);

        PackageSetup entity = PackageSetup.builder()
                .code(code)
                .name(vm.name().trim())
                .description(blankToNull(vm.description()))
                .price(vm.price())
                .isActive(vm.isActive() == null || vm.isActive())
                .validFrom(vm.validFrom())
                .validTo(vm.validTo())
                .discountId(normalizeDiscountId(vm.discountId()))
                .allDepartments(!Boolean.FALSE.equals(vm.allDepartments()))
                .overrideDefaultService(Boolean.TRUE.equals(vm.overrideDefaultService()))
                .build();
        PackageSetup saved = packageSetupRepository.saveAndFlush(entity);

        saveItems(saved.getId(), vm.items());
        saveDepartments(saved.getId(), vm);
        return response(saved);
    }

    public PackageSetupResponseVM update(Long id, PackageSetupSaveVM vm) {
        LOG.info("[UPDATE] Package id={} items={}", id, vm.items() != null ? vm.items().size() : 0);
        PackageSetup entity = getOrThrow(id);
        String code = vm.code().trim();
        if (packageSetupRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new BadRequestAlertException("A package with this code already exists", ENTITY, "code.exists");
        }
        validateHeader(vm);

        entity.setCode(code);
        entity.setName(vm.name().trim());
        entity.setDescription(blankToNull(vm.description()));
        entity.setPrice(vm.price());
        if (vm.isActive() != null) {
            entity.setIsActive(vm.isActive());
        }
        entity.setValidFrom(vm.validFrom());
        entity.setValidTo(vm.validTo());
        entity.setDiscountId(normalizeDiscountId(vm.discountId()));
        entity.setAllDepartments(!Boolean.FALSE.equals(vm.allDepartments()));
        entity.setOverrideDefaultService(Boolean.TRUE.equals(vm.overrideDefaultService()));
        PackageSetup saved = packageSetupRepository.saveAndFlush(entity);

        // Items and departments are replaced as a whole: the screen always sends the full lists.
        packageSetupItemRepository.deleteByPackageSetupId(id);
        saveItems(id, vm.items());
        saveDepartments(id, vm);
        return response(saved);
    }

    public PackageSetupResponseVM toggleActive(Long id) {
        PackageSetup entity = getOrThrow(id);
        entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
        return response(packageSetupRepository.saveAndFlush(entity));
    }

    /** Checks the lines, takes code and name from each item's own setup table, and stores them. */
    private List<PackageSetupItem> saveItems(Long packageSetupId, List<PackageSetupItemVM> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BadRequestAlertException("A package needs at least one item", ENTITY, "items.required");
        }

        Set<String> seen = new HashSet<>();
        Map<PriceListItemType, Set<Long>> idsByType = new EnumMap<>(PriceListItemType.class);
        for (PackageSetupItemVM line : lines) {
            boolean category = Boolean.TRUE.equals(line.isCategory());
            if (line.itemType() == null || (!category && line.sourceId() == null)) {
                throw new BadRequestAlertException("Each item needs a type and an item", ENTITY, "item.required");
            }
            if (line.quantity() == null || line.quantity() < 1) {
                throw new BadRequestAlertException("Quantity must be at least 1", ENTITY, "item.quantity");
            }
            if (line.maxRepeat() != null && line.maxRepeat() < 1) {
                throw new BadRequestAlertException("Repeat must be at least 1", ENTITY, "item.repeat");
            }
            String key = category ? line.itemType() + ":ALL" : line.itemType() + ":" + line.sourceId();
            if (!seen.add(key)) {
                throw new BadRequestAlertException(
                        category ? "The same category is added twice" : "The same item is added twice", ENTITY, "item.duplicate");
            }
            if (!category) {
                idsByType.computeIfAbsent(line.itemType(), t -> new HashSet<>()).add(line.sourceId());
            }
        }
        if (lines.stream().allMatch(l -> Boolean.FALSE.equals(l.isActive()))) {
            throw new BadRequestAlertException("A package needs at least one active item", ENTITY, "items.noneActive");
        }

        Map<PriceListItemType, Map<Long, PackageItemLookupVM>> found = new EnumMap<>(PriceListItemType.class);
        idsByType.forEach((type, ids) -> found.put(type, packageItemLookupService.findByIds(type, ids)));

        List<PackageSetupItem> toSave = new ArrayList<>();
        for (PackageSetupItemVM line : lines) {
            boolean category = Boolean.TRUE.equals(line.isCategory());
            PackageItemLookupVM source = category ? null : found.get(line.itemType()).get(line.sourceId());
            if (!category && source == null) {
                throw new BadRequestAlertException(
                        "Item not found: " + line.itemType() + " id " + line.sourceId(), ENTITY, "item.notfound");
            }
            toSave.add(PackageSetupItem.builder()
                    .packageSetupId(packageSetupId)
                    .itemType(line.itemType())
                    .sourceId(category ? null : line.sourceId())
                    .itemCode(category ? null : source.code())
                    .itemName(category ? null : source.name())
                    .quantity(line.quantity())
                    .isActive(!Boolean.FALSE.equals(line.isActive()))
                    .isCategory(category)
                    .maxRepeat(category ? line.maxRepeat() : null)
                    .discountId(normalizeDiscountId(line.discountId()))
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
