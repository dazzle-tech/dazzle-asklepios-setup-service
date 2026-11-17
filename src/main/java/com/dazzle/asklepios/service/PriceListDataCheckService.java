package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListDataCheckVM.CheckCountVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListDataCheckVM.RowPageVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListDataCheckVM.RowVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListDataCheckVM.SummaryVM;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Data checks over the price lists visible to a facility and the catalog items they point at
 * (procedure, service, brand_medication, vaccine_brands, diagnostic_test).
 * "Live" lists are active with status ACTIVE; "cash" lists are CASH or SELF_PAY.
 * Each check returns rows of the same shape, so the UI can list and export any of them.
 */
@Service
@Transactional(readOnly = true)
public class PriceListDataCheckService {

    // Large enough for the UI export of a whole check
    private static final int MAX_PAGE_SIZE = 10000;

    private static final String BASE = """
            with lists as (
                select pls.id, pls.name, pls.type,
                       (pls.is_active and pls.status = 'ACTIVE') as live
                from price_list_setup pls
                where pls.facility_id = :facilityId or pls.applies_to_all_facilities
            ),
            catalog as (
                select 'PROCEDURE' as item_type, p.id, p.code, p.name, p.price,
                       coalesce(p.is_active, false) as active, p.facility_id
                from procedure p
                union all
                select 'SERVICE', s.id, s.code, s.name, s.price, coalesce(s.is_active, false), s.facility_id
                from service s
                union all
                select 'MEDICATION', b.id, b.code, b.name, b.price, coalesce(b.is_active, false), b.facility_id
                from brand_medication b
                union all
                select 'VACCINE', v.id, v.code, v.name, v.price, coalesce(v.is_active, false), v.facility_id
                from vaccine_brands v
                union all
                select t.type, t.id, t.internal_code, t.name, t.price, coalesce(t.is_active, false), null
                from diagnostic_test t
                where t.type in ('LABORATORY', 'RADIOLOGY', 'PATHOLOGY')
            )
            """;

    public enum Check {
        /** Active price list item whose catalog item is inactive. */
        INACTIVE_SOURCE_ACTIVE_ITEM("""
                select i.item_type, i.source_id, i.item_code, i.item_name, l.id as price_list_id,
                       l.name as price_list_name, i.id as item_id, i.unit_price, c.price as setup_price,
                       'Setup item is inactive' as detail
                from price_list_setup_item i
                join lists l on l.id = i.price_list_setup_id
                join catalog c on c.item_type = i.item_type and c.id = i.source_id
                where i.is_active and not c.active
                """),
        /** Active catalog item of the facility with no active item in a live cash list. */
        MISSING_FROM_CASH("""
                select c.item_type, c.id as source_id, c.code as item_code, c.name as item_name,
                       null::bigint as price_list_id, null as price_list_name, null::bigint as item_id,
                       null::numeric as unit_price, c.price as setup_price,
                       case when coalesce(c.price, 0) = 0 then 'Setup price is 0' end as detail
                from catalog c
                where c.active
                  and (c.facility_id is null or c.facility_id = :facilityId)
                  and not exists (
                        select 1
                        from price_list_setup_item i
                        join lists l on l.id = i.price_list_setup_id
                        where l.live and l.type in ('CASH', 'SELF_PAY') and i.is_active
                          and i.item_type = c.item_type and i.source_id = c.id)
                """),
        /** Active item in a live cash list whose price differs from the catalog item price. */
        CASH_PRICE_DIFFERS("""
                select i.item_type, i.source_id, i.item_code, i.item_name, l.id as price_list_id,
                       l.name as price_list_name, i.id as item_id, i.unit_price, c.price as setup_price,
                       'Difference ' || (i.unit_price - c.price)::text as detail
                from price_list_setup_item i
                join lists l on l.id = i.price_list_setup_id
                join catalog c on c.item_type = i.item_type and c.id = i.source_id
                where l.live and l.type in ('CASH', 'SELF_PAY') and i.is_active
                  and c.price is not null and i.unit_price is distinct from c.price
                """),
        /** Active item in a live insurance list with no SBS code. */
        INSURANCE_WITHOUT_SBS("""
                select i.item_type, i.source_id, i.item_code, i.item_name, l.id as price_list_id,
                       l.name as price_list_name, i.id as item_id, i.unit_price, c.price as setup_price,
                       'No SBS code' as detail
                from price_list_setup_item i
                join lists l on l.id = i.price_list_setup_id
                left join catalog c on c.item_type = i.item_type and c.id = i.source_id
                where l.live and l.type = 'INSURANCE' and i.is_active and i.sbs_catalog_id is null
                """),
        /** Active item in a live list with price 0 or no price. */
        ZERO_PRICE("""
                select i.item_type, i.source_id, i.item_code, i.item_name, l.id as price_list_id,
                       l.name as price_list_name, i.id as item_id, i.unit_price, c.price as setup_price,
                       case when i.unit_price is null then 'No price' else 'Price is 0' end as detail
                from price_list_setup_item i
                join lists l on l.id = i.price_list_setup_id
                left join catalog c on c.item_type = i.item_type and c.id = i.source_id
                where l.live and i.is_active and coalesce(i.unit_price, 0) = 0
                """),
        /** The same catalog item more than once (active) in one list. */
        DUPLICATE_IN_LIST("""
                select i.item_type, i.source_id, min(i.item_code) as item_code, min(i.item_name) as item_name,
                       l.id as price_list_id, l.name as price_list_name, min(i.id) as item_id,
                       min(i.unit_price) as unit_price, null::numeric as setup_price,
                       count(*) || ' active rows: ' || string_agg(i.id::text, ', ' order by i.id) as detail
                from price_list_setup_item i
                join lists l on l.id = i.price_list_setup_id
                where i.is_active
                group by i.item_type, i.source_id, l.id, l.name
                having count(*) > 1
                """),
        /** Active price list item whose catalog item no longer exists. */
        ORPHAN_ITEM("""
                select i.item_type, i.source_id, i.item_code, i.item_name, l.id as price_list_id,
                       l.name as price_list_name, i.id as item_id, i.unit_price, null::numeric as setup_price,
                       'Setup item not found' as detail
                from price_list_setup_item i
                join lists l on l.id = i.price_list_setup_id
                where i.is_active
                  and not exists (select 1 from catalog c where c.item_type = i.item_type and c.id = i.source_id)
                """);

        private final String sql;

        Check(String sql) {
            this.sql = sql;
        }

        public static Optional<Check> of(String key) {
            return Arrays.stream(values()).filter(check -> check.name().equalsIgnoreCase(key)).findFirst();
        }
    }

    private final NamedParameterJdbcTemplate jdbc;

    public PriceListDataCheckService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public SummaryVM summary(Long facilityId, PriceListItemType itemType) {
        MapSqlParameterSource params = params(facilityId, itemType);
        List<CheckCountVM> counts = new ArrayList<>();
        for (Check check : Check.values()) {
            Long count = jdbc.queryForObject(
                    BASE + "select count(*) from (" + check.sql + ") x" + typeFilter(),
                    params,
                    Long.class
            );
            counts.add(new CheckCountVM(check.name(), count == null ? 0 : count));
        }
        return new SummaryVM(counts);
    }

    public RowPageVM rows(
            Long facilityId,
            Check check,
            PriceListItemType itemType,
            String search,
            int page,
            int size
    ) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        String term = search == null || search.isBlank() ? null : "%" + search.trim() + "%";
        MapSqlParameterSource params = params(facilityId, itemType)
                .addValue("search", term)
                .addValue("limit", safeSize)
                .addValue("offset", (long) safePage * safeSize);
        String filter = typeFilter()
                + " and (cast(:search as text) is null"
                + " or x.item_code ilike cast(:search as text) or x.item_name ilike cast(:search as text))";

        Long total = jdbc.queryForObject(
                BASE + "select count(*) from (" + check.sql + ") x" + filter,
                params,
                Long.class
        );
        long totalElements = total == null ? 0 : total;
        List<RowVM> content = jdbc.query(
                BASE + "select * from (" + check.sql + ") x" + filter
                        + " order by x.item_type, x.item_name, x.price_list_name, x.item_id limit :limit offset :offset",
                params,
                (rs, rowNum) -> toRow(rs)
        );
        int totalPages = (int) ((totalElements + safeSize - 1) / safeSize);
        return new RowPageVM(check.name(), content, safePage, safeSize, totalElements, totalPages);
    }

    private static MapSqlParameterSource params(Long facilityId, PriceListItemType itemType) {
        return new MapSqlParameterSource()
                .addValue("facilityId", facilityId)
                .addValue("itemType", itemType == null ? null : itemType.name());
    }

    private static String typeFilter() {
        return " where (cast(:itemType as text) is null or x.item_type = cast(:itemType as text))";
    }

    private static RowVM toRow(ResultSet rs) throws SQLException {
        return new RowVM(
                itemType(rs.getString("item_type")),
                nullableLong(rs, "source_id"),
                rs.getString("item_code"),
                rs.getString("item_name"),
                nullableLong(rs, "price_list_id"),
                rs.getString("price_list_name"),
                nullableLong(rs, "item_id"),
                rs.getBigDecimal("unit_price"),
                rs.getBigDecimal("setup_price"),
                rs.getString("detail")
        );
    }

    private static PriceListItemType itemType(String value) {
        if (value == null) {
            return null;
        }
        try {
            return PriceListItemType.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}
