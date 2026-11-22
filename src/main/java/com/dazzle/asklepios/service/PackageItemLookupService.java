package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.web.rest.vm.packageSetup.PackageItemLookupVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Finds the items a package can hold, each in its own setup table:
 * LABORATORY / RADIOLOGY / PATHOLOGY -> diagnostic_test, SERVICE -> service, PROCEDURE -> procedure,
 * MEDICATION -> brand_medication, VACCINE -> vaccine_brands.
 */
@Service
@Transactional(readOnly = true)
public class PackageItemLookupService {

    private final NamedParameterJdbcTemplate jdbc;

    public PackageItemLookupService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Table, code column and extra condition for one item type. Values are fixed here, never from the request. */
    private record Source(String table, String codeColumn, String extraCondition) {}

    private static Source sourceOf(PriceListItemType type) {
        return switch (type) {
            case LABORATORY -> new Source("diagnostic_test", "internal_code", "t.type = 'LABORATORY'");
            case RADIOLOGY -> new Source("diagnostic_test", "internal_code", "t.type = 'RADIOLOGY'");
            case PATHOLOGY -> new Source("diagnostic_test", "internal_code", "t.type = 'PATHOLOGY'");
            case SERVICE -> new Source("service", "code", null);
            case PROCEDURE -> new Source("procedure", "code", null);
            case MEDICATION -> new Source("brand_medication", "code", null);
            case VACCINE -> new Source("vaccine_brands", "code", null);
        };
    }

    private static String select(Source source) {
        return "select t.id as source_id, t." + source.codeColumn() + " as code, t.name as name, t.price as price"
                + " from " + source.table() + " t where 1 = 1"
                + (source.extraCondition() != null ? " and " + source.extraCondition() : "");
    }

    private static PackageItemLookupVM map(PriceListItemType type, java.sql.ResultSet rs) throws java.sql.SQLException {
        return new PackageItemLookupVM(
                type,
                rs.getLong("source_id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getBigDecimal("price")
        );
    }

    /** Active items of one type whose name or code contains the search text. */
    public Page<PackageItemLookupVM> search(PriceListItemType type, String search, Pageable pageable) {
        Source source = sourceOf(type);
        StringBuilder where = new StringBuilder(" and coalesce(t.is_active, true) = true");
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (search != null && !search.isBlank()) {
            where.append(" and (t.name ilike :search or t.").append(source.codeColumn()).append(" ilike :search)");
            params.addValue("search", "%" + search.trim() + "%");
        }

        Long total = jdbc.queryForObject(
                "select count(*) from (" + select(source) + where + ") x", params, Long.class);

        params.addValue("limit", pageable.getPageSize());
        params.addValue("offset", pageable.getOffset());
        List<PackageItemLookupVM> content = jdbc.query(
                select(source) + where + " order by t.name, t.id limit :limit offset :offset",
                params,
                (rs, rowNum) -> map(type, rs));

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /** The items of one type with these ids, active or not, keyed by id. */
    public Map<Long, PackageItemLookupVM> findByIds(PriceListItemType type, Collection<Long> ids) {
        Map<Long, PackageItemLookupVM> result = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return result;
        }
        jdbc.query(
                select(sourceOf(type)) + " and t.id in (:ids)",
                new MapSqlParameterSource("ids", ids),
                rs -> {
                    PackageItemLookupVM vm = map(type, rs);
                    result.put(vm.sourceId(), vm);
                });
        return result;
    }
}
