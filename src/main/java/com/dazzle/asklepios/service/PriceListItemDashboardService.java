package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.Payor;
import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.PriceListSetupItem;
import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.PayorRepository;
import com.dazzle.asklepios.repository.PriceListSetupItemRepository;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemDashboardVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemDashboardVM.CatalogItemVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemDashboardVM.ItemCardVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemDashboardVM.ItemEntryVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemDashboardVM.ItemPageVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemDashboardVM.PriceListColumnVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemDashboardVM.PriceListCoverageVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemDashboardVM.SummaryVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemDashboardVM.TypeCountVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PriceListItemDashboardService {

    static final String COMPLETE = "COMPLETE";
    static final String PARTIAL = "PARTIAL";
    static final String PRESENT = "PRESENT";
    static final String MISSING = "MISSING";
    static final String INACTIVE = "INACTIVE";

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private static final Comparator<PriceListSetup> PRICE_LIST_ORDER =
            Comparator.comparing(PriceListSetup::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                    .thenComparing(PriceListSetup::getId, Comparator.nullsLast(Long::compareTo));

    private static final Comparator<ItemEntryVM> ENTRY_ORDER =
            Comparator.comparing(ItemEntryVM::id, Comparator.nullsLast(Long::compareTo));

    private final PriceListSetupRepository priceListSetupRepository;
    private final PriceListSetupItemRepository priceListSetupItemRepository;
    private final PayorRepository payorRepository;
    private final NphiesPayerRepository nphiesPayerRepository;
    private final FacilityRepository facilityRepository;

    public PriceListItemDashboardService(
            PriceListSetupRepository priceListSetupRepository,
            PriceListSetupItemRepository priceListSetupItemRepository,
            PayorRepository payorRepository,
            NphiesPayerRepository nphiesPayerRepository,
            FacilityRepository facilityRepository
    ) {
        this.priceListSetupRepository = priceListSetupRepository;
        this.priceListSetupItemRepository = priceListSetupItemRepository;
        this.payorRepository = payorRepository;
        this.nphiesPayerRepository = nphiesPayerRepository;
        this.facilityRepository = facilityRepository;
    }

    public PriceListItemDashboardVM overview(Long facilityId) {
        List<PriceListSetup> priceLists = loadPriceLists(facilityId);
        List<PriceListColumnVM> columns = toColumns(priceLists);
        if (priceLists.isEmpty()) {
            return new PriceListItemDashboardVM(new SummaryVM(0, 0, 0, 0, List.of()), columns);
        }

        List<Long> priceListIds = idsOf(priceLists);
        int itemCount = (int) priceListSetupItemRepository.countByPriceListSetupIdIn(priceListIds);
        List<TypeCountVM> byItemType = new ArrayList<>();
        for (PriceListItemType type : PriceListItemType.values()) {
            int typeCount = (int) priceListSetupItemRepository.countByPriceListSetupIdInAndItemType(
                    priceListIds,
                    type
            );
            if (typeCount > 0) {
                byItemType.add(new TypeCountVM(type, typeCount));
            }
        }
        return new PriceListItemDashboardVM(
                new SummaryVM(priceLists.size(), itemCount, 0, 0, byItemType),
                columns
        );
    }

    public ItemPageVM searchItems(
            Long facilityId,
            String search,
            PriceListItemType itemType,
            Pageable pageable
    ) {
        Pageable safe = safePageable(pageable);
        List<PriceListSetup> priceLists = loadPriceLists(facilityId);
        if (priceLists.isEmpty()) {
            return emptyPage(safe);
        }

        List<Long> priceListIds = idsOf(priceLists);
        Page<PriceListSetupItem> page = searchPage(priceListIds, normalize(search), itemType, safe);
        List<CatalogItemVM> content = toCatalogItems(page.getContent(), priceListIds, priceLists.size());
        return new ItemPageVM(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public Optional<ItemCardVM> findItem(Long facilityId, PriceListItemType itemType, Long sourceId) {
        if (itemType == null || sourceId == null) {
            return Optional.empty();
        }

        List<PriceListSetup> priceLists = loadPriceLists(facilityId);
        if (priceLists.isEmpty()) {
            return Optional.empty();
        }

        List<Long> priceListIds = idsOf(priceLists);
        List<PriceListSetupItem> matches =
                priceListSetupItemRepository.findAllByPriceListSetupIdInAndItemTypeAndSourceId(
                        priceListIds,
                        itemType,
                        sourceId
                );
        if (matches.isEmpty()) {
            return Optional.empty();
        }

        Map<Long, List<PriceListSetupItem>> entriesByListId = matches.stream()
                .filter(item -> item.getPriceListSetupId() != null)
                .collect(Collectors.groupingBy(PriceListSetupItem::getPriceListSetupId));
        List<PriceListColumnVM> columns = toColumns(priceLists);
        Map<Long, PriceListColumnVM> columnsById = columns.stream()
                .collect(Collectors.toMap(PriceListColumnVM::id, column -> column, (left, right) -> left, LinkedHashMap::new));

        List<PriceListCoverageVM> coverage = new ArrayList<>();
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        int presentInCount = 0;
        PriceListSetupItem sample = matches.getFirst();

        for (PriceListSetup priceList : priceLists) {
            List<ItemEntryVM> entries = entriesByListId.getOrDefault(priceList.getId(), List.of()).stream()
                    .map(this::toEntry)
                    .sorted(ENTRY_ORDER)
                    .toList();
            boolean present = !entries.isEmpty();
            if (present) {
                presentInCount++;
                for (ItemEntryVM entry : entries) {
                    minPrice = min(minPrice, entry.unitPrice());
                    maxPrice = max(maxPrice, entry.unitPrice());
                }
            }
            coverage.add(new PriceListCoverageVM(
                    columnsById.get(priceList.getId()),
                    present,
                    presenceOf(entries),
                    entries
            ));
        }

        int missingFromCount = Math.max(priceLists.size() - presentInCount, 0);
        ItemEntryVM identity = coverage.stream()
                .flatMap(row -> row.entries().stream())
                .findFirst()
                .orElseGet(() -> toEntry(sample));
        return Optional.of(new ItemCardVM(
                itemType,
                sourceId,
                identity.itemCode(),
                identity.itemName(),
                identity.category(),
                identity.nonStandardCode(),
                presentInCount,
                missingFromCount,
                coverageStatus(presentInCount, priceLists.size()),
                coveragePercent(presentInCount, priceLists.size()),
                hasVariance(minPrice, maxPrice),
                minPrice,
                maxPrice,
                coverage
        ));
    }

    private List<PriceListSetup> loadPriceLists(Long facilityId) {
        return priceListSetupRepository.listAllVisibleToFacility(facilityId).stream()
                .sorted(PRICE_LIST_ORDER)
                .toList();
    }

    private List<PriceListColumnVM> toColumns(List<PriceListSetup> priceLists) {
        if (priceLists.isEmpty()) {
            return List.of();
        }
        Lookups lookups = loadLookups(priceLists);
        return priceLists.stream()
                .map(list -> toColumn(list, lookups))
                .toList();
    }

    private Lookups loadLookups(List<PriceListSetup> priceLists) {
        Set<Long> payerIds = priceLists.stream()
                .map(PriceListSetup::getPayerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> nphiesPayerIds = priceLists.stream()
                .map(PriceListSetup::getNphiesPayerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> facilityIds = priceLists.stream()
                .map(PriceListSetup::getFacilityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> payerNames = new HashMap<>();
        if (!payerIds.isEmpty()) {
            for (Payor payer : payorRepository.findAllById(payerIds)) {
                if (payer.getId() != null) {
                    payerNames.put(payer.getId(), payer.getName());
                }
            }
        }
        Map<Long, String> nphiesPayerNames = new HashMap<>();
        if (!nphiesPayerIds.isEmpty()) {
            for (NphiesPayer payer : nphiesPayerRepository.findByIdIn(nphiesPayerIds)) {
                if (payer.getId() != null) {
                    nphiesPayerNames.put(payer.getId(), nphiesPayerName(payer));
                }
            }
        }
        Map<Long, String> facilityNames = new HashMap<>();
        if (!facilityIds.isEmpty()) {
            for (Facility facility : facilityRepository.findAllById(facilityIds)) {
                if (facility.getId() != null) {
                    facilityNames.put(facility.getId(), facility.getName());
                }
            }
        }
        return new Lookups(payerNames, nphiesPayerNames, facilityNames);
    }

    private PriceListColumnVM toColumn(PriceListSetup list, Lookups lookups) {
        return new PriceListColumnVM(
                list.getId(),
                list.getName(),
                list.getShortName(),
                list.getDescription(),
                list.getType(),
                list.getStatus(),
                list.getIsActive(),
                list.getCurrency(),
                list.getVersionNumber(),
                list.getEffectiveFrom(),
                list.getEffectiveTo(),
                list.getFacilityId(),
                Boolean.TRUE.equals(list.getAppliesToAllFacilities())
                        ? "All"
                        : lookups.facilityNames().get(list.getFacilityId()),
                list.getAppliesToAllFacilities(),
                list.getPayerId(),
                lookups.payerNames().get(list.getPayerId()),
                list.getNphiesPayerId(),
                lookups.nphiesPayerNames().get(list.getNphiesPayerId())
        );
    }

    private Page<PriceListSetupItem> searchPage(
            List<Long> priceListIds,
            String search,
            PriceListItemType itemType,
            Pageable pageable
    ) {
        boolean byCode = searchByCode(search);
        if (search.isBlank() && itemType == null) {
            return priceListSetupItemRepository.findAllByPriceListSetupIdIn(priceListIds, pageable);
        }
        if (search.isBlank()) {
            return priceListSetupItemRepository.findAllByPriceListSetupIdInAndItemType(
                    priceListIds,
                    itemType,
                    pageable
            );
        }
        if (itemType == null) {
            return byCode
                    ? priceListSetupItemRepository.findAllByPriceListSetupIdInAndItemCodeContainingIgnoreCase(
                            priceListIds,
                            search,
                            pageable
                    )
                    : priceListSetupItemRepository.findAllByPriceListSetupIdInAndItemNameContainingIgnoreCase(
                            priceListIds,
                            search,
                            pageable
                    );
        }
        return byCode
                ? priceListSetupItemRepository.findAllByPriceListSetupIdInAndItemTypeAndItemCodeContainingIgnoreCase(
                        priceListIds,
                        itemType,
                        search,
                        pageable
                )
                : priceListSetupItemRepository.findAllByPriceListSetupIdInAndItemTypeAndItemNameContainingIgnoreCase(
                        priceListIds,
                        itemType,
                        search,
                        pageable
                );
    }

    private List<CatalogItemVM> toCatalogItems(
            List<PriceListSetupItem> pageItems,
            List<Long> priceListIds,
            int priceListCount
    ) {
        LinkedHashMap<String, PriceListSetupItem> unique = new LinkedHashMap<>();
        for (PriceListSetupItem item : pageItems) {
            if (item.getItemType() == null || item.getSourceId() == null) {
                continue;
            }
            unique.putIfAbsent(catalogKey(item.getItemType(), item.getSourceId()), item);
        }
        if (unique.isEmpty()) {
            return List.of();
        }

        Set<PriceListItemType> itemTypes = unique.values().stream()
                .map(PriceListSetupItem::getItemType)
                .collect(Collectors.toSet());
        Set<Long> sourceIds = unique.values().stream()
                .map(PriceListSetupItem::getSourceId)
                .collect(Collectors.toSet());
        Map<String, List<PriceListSetupItem>> coverageByKey =
                priceListSetupItemRepository.findAllByPriceListSetupIdInAndItemTypeInAndSourceIdIn(
                                priceListIds,
                                itemTypes,
                                sourceIds
                        )
                        .stream()
                        .filter(item -> item.getItemType() != null && item.getSourceId() != null)
                        .collect(Collectors.groupingBy(item -> catalogKey(item.getItemType(), item.getSourceId())));

        List<CatalogItemVM> catalogItems = new ArrayList<>(unique.size());
        for (Map.Entry<String, PriceListSetupItem> entry : unique.entrySet()) {
            catalogItems.add(toCatalogItem(
                    entry.getValue(),
                    coverageByKey.getOrDefault(entry.getKey(), List.of(entry.getValue())),
                    priceListCount
            ));
        }
        return catalogItems;
    }

    private CatalogItemVM toCatalogItem(
            PriceListSetupItem sample,
            List<PriceListSetupItem> coverage,
            int priceListCount
    ) {
        Set<Long> presentIds = coverage.stream()
                .map(PriceListSetupItem::getPriceListSetupId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        int presentInCount = presentIds.size();
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        for (PriceListSetupItem item : coverage) {
            minPrice = min(minPrice, item.getUnitPrice());
            maxPrice = max(maxPrice, item.getUnitPrice());
        }
        return new CatalogItemVM(
                sample.getItemType(),
                sample.getSourceId(),
                sample.getItemCode(),
                sample.getItemName(),
                sample.getCategory(),
                sample.getNonStandardCode(),
                presentInCount,
                Math.max(priceListCount - presentInCount, 0),
                coverageStatus(presentInCount, priceListCount),
                coveragePercent(presentInCount, priceListCount),
                minPrice,
                maxPrice,
                hasVariance(minPrice, maxPrice)
        );
    }

    private ItemEntryVM toEntry(PriceListSetupItem item) {
        return new ItemEntryVM(
                item.getId(),
                item.getPriceListSetupId(),
                item.getWaseelItemMappingId(),
                item.getSbsCatalogId(),
                item.getItemCode(),
                item.getNonStandardCode(),
                item.getItemName(),
                item.getCategory(),
                item.getUnitPrice(),
                item.getDiscountPercentage(),
                netPrice(item.getUnitPrice(), item.getDiscountPercentage()),
                item.getIsActive(),
                item.getRequiresPreAuthorization(),
                item.getCreatedDate(),
                item.getLastModifiedDate()
        );
    }

    private ItemPageVM emptyPage(Pageable pageable) {
        Pageable safe = safePageable(pageable);
        return new ItemPageVM(List.of(), safe.getPageNumber(), safe.getPageSize(), 0, 0);
    }

    private Pageable safePageable(Pageable pageable) {
        int size = DEFAULT_PAGE_SIZE;
        int page = 0;
        if (pageable != null && !pageable.isUnpaged()) {
            size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
            page = Math.max(pageable.getPageNumber(), 0);
        }
        return PageRequest.of(page, size, Sort.by(Sort.Order.asc("itemName"), Sort.Order.asc("id")));
    }

    private List<Long> idsOf(List<PriceListSetup> priceLists) {
        return priceLists.stream().map(PriceListSetup::getId).filter(Objects::nonNull).toList();
    }

    private String nphiesPayerName(NphiesPayer payer) {
        if (payer.getNameEn() != null && !payer.getNameEn().isBlank()) {
            return payer.getNameEn();
        }
        return payer.getNameAr();
    }

    private static String presenceOf(List<ItemEntryVM> entries) {
        if (entries.isEmpty()) {
            return MISSING;
        }
        boolean anyActive = entries.stream().anyMatch(entry -> Boolean.TRUE.equals(entry.isActive()));
        return anyActive ? PRESENT : INACTIVE;
    }

    private static String coverageStatus(int presentInCount, int priceListCount) {
        return presentInCount >= priceListCount && priceListCount > 0 ? COMPLETE : PARTIAL;
    }

    private static int coveragePercent(int presentInCount, int priceListCount) {
        if (priceListCount <= 0) {
            return 0;
        }
        return (int) Math.round(presentInCount * 100.0 / priceListCount);
    }

    private static boolean hasVariance(BigDecimal minPrice, BigDecimal maxPrice) {
        return minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) != 0;
    }

    private static BigDecimal min(BigDecimal current, BigDecimal candidate) {
        if (candidate == null) {
            return current;
        }
        return current == null || candidate.compareTo(current) < 0 ? candidate : current;
    }

    private static BigDecimal max(BigDecimal current, BigDecimal candidate) {
        if (candidate == null) {
            return current;
        }
        return current == null || candidate.compareTo(current) > 0 ? candidate : current;
    }

    private static BigDecimal netPrice(BigDecimal unitPrice, BigDecimal discountPercentage) {
        if (unitPrice == null) {
            return null;
        }
        BigDecimal discount = discountPercentage == null ? BigDecimal.ZERO : discountPercentage;
        if (discount.compareTo(BigDecimal.ZERO) == 0) {
            return unitPrice;
        }
        return unitPrice.subtract(unitPrice.multiply(discount).divide(HUNDRED, 4, RoundingMode.HALF_UP));
    }

    private static String catalogKey(PriceListItemType itemType, Long sourceId) {
        return itemType.name() + ":" + sourceId;
    }

    private static boolean searchByCode(String search) {
        if (search.isBlank() || search.indexOf(' ') >= 0) {
            return false;
        }
        boolean hasDigit = false;
        boolean hasLetter = false;
        boolean allUpper = true;
        for (int i = 0; i < search.length(); i++) {
            char character = search.charAt(i);
            if (Character.isDigit(character)) {
                hasDigit = true;
            } else if (Character.isLetter(character)) {
                hasLetter = true;
                if (!Character.isUpperCase(character)) {
                    allUpper = false;
                }
            }
        }
        return hasDigit || (hasLetter && allUpper);
    }

    private static String normalize(String search) {
        return search == null ? "" : search.trim();
    }

    private record Lookups(
            Map<Long, String> payerNames,
            Map<Long, String> nphiesPayerNames,
            Map<Long, String> facilityNames
    ) {}
}
