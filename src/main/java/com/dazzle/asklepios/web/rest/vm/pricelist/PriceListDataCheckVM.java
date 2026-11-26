package com.dazzle.asklepios.web.rest.vm.pricelist;

import com.dazzle.asklepios.domain.enumeration.PriceListItemType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data checks of the price lists visible to a facility (Item Price Dashboard, "Data checks" tab).
 */
public final class PriceListDataCheckVM {

    private PriceListDataCheckVM() {}

    public record SummaryVM(
            List<CheckCountVM> checks
    ) {}

    public record CheckCountVM(
            String key,
            long count
    ) {}

    public record RowPageVM(
            String key,
            List<RowVM> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}

    public record RowVM(
            PriceListItemType itemType,
            Long sourceId,
            String itemCode,
            String itemName,
            Long priceListId,
            String priceListName,
            Long priceListItemId,
            BigDecimal unitPrice,
            BigDecimal setupPrice,
            String detail
    ) {}
}
