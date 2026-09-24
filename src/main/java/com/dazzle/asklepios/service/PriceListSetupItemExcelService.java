package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.PriceListSetupItem;
import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.WaseelItemMapping;
import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupType;
import com.dazzle.asklepios.domain.enumeration.TestType;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.repository.BrandMedicationRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.PriceListSetupItemRepository;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.repository.ProcedureRepository;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.repository.WaseelItemMappingRepository;
import com.dazzle.asklepios.service.dto.PriceListSetupItemDTO;
import com.dazzle.asklepios.service.dto.PriceListSetupItemImportErrorDTO;
import com.dazzle.asklepios.service.dto.PriceListSetupItemImportResultDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class PriceListSetupItemExcelService {

    private static final String ENTITY = "priceListSetupItem";

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    private static final String[] HEADERS = {
            "itemType",
            "itemCode",
            "itemName",
            "sourceId",
            "category",
            "unitPrice",
            "discountPercentage",
            "isActive",
            "requiresPreAuthorization",
            "waseelItemMappingId",
            "sbsCatalogId"
    };

    private static final Set<String> REQUIRED_HEADERS = Set.of(
            "itemtype",
            "itemcode",
            "unitprice"
    );

    private static final Map<String, Set<String>> HEADER_ALIASES = Map.ofEntries(
            Map.entry("itemtype", Set.of("itemtype", "item type", "item_type", "type")),
            Map.entry("itemcode", Set.of("itemcode", "item code", "item_code", "code")),
            Map.entry("itemname", Set.of("itemname", "item name", "item_name", "name")),
            Map.entry("sourceid", Set.of("sourceid", "source id", "source_id", "source")),
            Map.entry("category", Set.of("category", "service category")),
            Map.entry("unitprice", Set.of("unitprice", "unit price", "unit_price", "price")),
            Map.entry("discountpercentage", Set.of(
                    "discountpercentage",
                    "discount percentage",
                    "discount_percentage",
                    "discount"
            )),
            Map.entry("isactive", Set.of("isactive", "is active", "is_active", "active")),
            Map.entry("requirespreauthorization", Set.of(
                    "requirespreauthorization",
                    "requires preauthorization",
                    "requires_pre_authorization",
                    "preauth",
                    "pre-authorization"
            )),
            Map.entry("waseelitemmappingid", Set.of(
                    "waseelitemmappingid",
                    "waseel item mapping id",
                    "waseel_item_mapping_id",
                    "mappingid",
                    "mapping id"
            )),
            Map.entry("sbscatalogid", Set.of(
                    "sbscatalogid",
                    "sbs catalog id",
                    "sbs_catalog_id",
                    "sbs"
            ))
    );

    private final PriceListSetupRepository priceListSetupRepository;

    private final PriceListSetupItemRepository priceListSetupItemRepository;

    private final PriceListSetupItemService priceListSetupItemService;

    private final ServiceRepository serviceRepository;

    private final ProcedureRepository procedureRepository;

    private final BrandMedicationRepository brandMedicationRepository;

    private final DiagnosticTestRepository diagnosticTestRepository;

    private final WaseelItemMappingRepository waseelItemMappingRepository;

    public byte[] buildTemplate(Long priceListSetupId) {
        requirePriceList(priceListSetupId);
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet itemsSheet = workbook.createSheet("Items");
            writeHeaderRow(itemsSheet);

            Sheet instructionsSheet = workbook.createSheet("Instructions");
            writeInstructions(instructionsSheet);

            for (int index = 0; index < HEADERS.length; index++) {
                itemsSheet.autoSizeColumn(index);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new BadRequestAlertException(
                    "Unable to generate the price-list items template.",
                    ENTITY,
                    "item.import.templateFailed"
            );
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportItems(Long priceListSetupId) {
        PriceListSetup priceList = requirePriceList(priceListSetupId);

        List<PriceListSetupItem> items =
                priceListSetupItemRepository.findAllByPriceListSetupId(
                        priceList.getId()
                );

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet itemsSheet = workbook.createSheet("Items");
            writeHeaderRow(itemsSheet);

            int rowIndex = 1;
            for (PriceListSetupItem item : items) {
                Row row = itemsSheet.createRow(rowIndex++);
                writeItemRow(row, item);
            }

            Sheet instructionsSheet = workbook.createSheet("Instructions");
            writeInstructions(instructionsSheet);

            for (int index = 0; index < HEADERS.length; index++) {
                itemsSheet.autoSizeColumn(index);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new BadRequestAlertException(
                    "Unable to export price-list items.",
                    ENTITY,
                    "item.export.failed"
            );
        }
    }

    public PriceListSetupItemImportResultDTO importItems(
            Long priceListSetupId,
            MultipartFile file
    ) {
        PriceListSetup priceList = requirePriceList(priceListSetupId);
        validateUploadedFile(file);

        List<ImportRow> rows = isExcelFile(file)
                ? readExcel(file)
                : readCsv(file);

        if (rows.isEmpty()) {
            throw new BadRequestAlertException(
                    "The file has no data rows.",
                    ENTITY,
                    "item.import.empty"
            );
        }

        int inserted = 0;
        int updated = 0;
        List<PriceListSetupItemImportErrorDTO> errors = new ArrayList<>();
        Set<String> itemCodesInFile = new HashSet<>();

        for (ImportRow row : rows) {
            try {
                String itemCodeKey = row.itemCode().toLowerCase(Locale.ROOT);

                if (!itemCodesInFile.add(itemCodeKey)) {
                    throw new BadRequestAlertException(
                            "Duplicate item code in the file: " + row.itemCode(),
                            ENTITY,
                            "itemCode.duplicate"
                    );
                }

                ResolvedCatalog catalog = resolveCatalog(priceList, row);
                ResolvedMapping mapping = resolveMapping(priceList, row, catalog);

                Optional<PriceListSetupItem> existing =
                        findExistingItem(priceListSetupId, row, catalog);

                PriceListSetupItemDTO dto = new PriceListSetupItemDTO(
                        existing.map(PriceListSetupItem::getId).orElse(null),
                        priceListSetupId,
                        mapping.waseelItemMappingId(),
                        mapping.sbsCatalogId(),
                        row.itemType(),
                        catalog.sourceId(),
                        catalog.itemCode(),
                        existing.map(PriceListSetupItem::getNonStandardCode).orElse(null),
                        catalog.itemName(),
                        row.category(),
                        row.unitPrice(),
                        row.discountPercentage(),
                        row.isActive(),
                        mapping.requiresPreAuthorization(),
                        null,
                        null,
                        null,
                        null
                );

                if (existing.isPresent()) {
                    priceListSetupItemService.update(
                            priceListSetupId,
                            existing.get().getId(),
                            dto
                    );
                    updated++;
                } else {
                    priceListSetupItemService.create(
                            priceListSetupId,
                            dto
                    );
                    inserted++;
                }
            } catch (RuntimeException exception) {
                errors.add(new PriceListSetupItemImportErrorDTO(
                        row.rowNumber(),
                        row.itemCode(),
                        exceptionMessage(exception)
                ));
            }
        }

        return new PriceListSetupItemImportResultDTO(
                rows.size(),
                inserted,
                updated,
                errors.size(),
                errors
        );
    }

    private PriceListSetup requirePriceList(Long priceListSetupId) {
        return priceListSetupRepository.findById(priceListSetupId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Price list setup not found with id: "
                                        + priceListSetupId
                        )
                );
    }

    private Optional<PriceListSetupItem> findExistingItem(
            Long priceListSetupId,
            ImportRow row,
            ResolvedCatalog catalog
    ) {
        return priceListSetupItemRepository
                .findFirstByPriceListSetupIdAndItemTypeAndSourceId(
                        priceListSetupId,
                        row.itemType(),
                        catalog.sourceId()
                );
    }

    private ResolvedCatalog resolveCatalog(
            PriceListSetup priceList,
            ImportRow row
    ) {
        if (row.sourceId() != null) {
            return new ResolvedCatalog(
                    row.sourceId(),
                    row.itemCode(),
                    firstNonBlank(row.itemName(), row.itemCode())
            );
        }

        Long facilityId = priceList.getFacilityId();

        return switch (row.itemType()) {
            case SERVICE -> serviceRepository
                    .findFirstByFacility_IdAndCodeIgnoreCase(
                            facilityId,
                            row.itemCode()
                    )
                    .map(service -> toCatalog(service, row))
                    .orElseThrow(() -> catalogNotFound(row));
            case PROCEDURE -> procedureRepository
                    .findFirstByFacility_IdAndCodeIgnoreCase(
                            facilityId,
                            row.itemCode()
                    )
                    .map(procedure -> toCatalog(procedure, row))
                    .orElseThrow(() -> catalogNotFound(row));
            case MEDICATION -> brandMedicationRepository
                    .findFirstByCodeIgnoreCase(row.itemCode())
                    .map(medication -> toCatalog(medication, row))
                    .orElseThrow(() -> catalogNotFound(row));
            case LABORATORY, RADIOLOGY, PATHOLOGY -> diagnosticTestRepository
                    .findFirstByInternalCodeIgnoreCaseAndType(
                            row.itemCode(),
                            TestType.valueOf(row.itemType().name())
                    )
                    .map(test -> toCatalog(test, row))
                    .orElseThrow(() -> catalogNotFound(row));
        };
    }

    private ResolvedCatalog toCatalog(ServiceSetup service, ImportRow row) {
        return new ResolvedCatalog(
                service.getId(),
                firstNonBlank(row.itemCode(), service.getCode()),
                firstNonBlank(row.itemName(), service.getName())
        );
    }

    private ResolvedCatalog toCatalog(Procedure procedure, ImportRow row) {
        return new ResolvedCatalog(
                procedure.getId(),
                firstNonBlank(row.itemCode(), procedure.getCode()),
                firstNonBlank(row.itemName(), procedure.getName())
        );
    }

    private ResolvedCatalog toCatalog(BrandMedication medication, ImportRow row) {
        return new ResolvedCatalog(
                medication.getId(),
                firstNonBlank(row.itemCode(), medication.getCode()),
                firstNonBlank(row.itemName(), medication.getName())
        );
    }

    private ResolvedCatalog toCatalog(DiagnosticTest test, ImportRow row) {
        return new ResolvedCatalog(
                test.getId(),
                firstNonBlank(row.itemCode(), test.getInternalCode()),
                firstNonBlank(row.itemName(), test.getName())
        );
    }

    private ResolvedMapping resolveMapping(
            PriceListSetup priceList,
            ImportRow row,
            ResolvedCatalog catalog
    ) {
        boolean insurance =
                priceList.getType() == PriceListSetupType.INSURANCE;

        if (!insurance) {
            if (Boolean.TRUE.equals(row.requiresPreAuthorization())) {
                throw new BadRequestAlertException(
                        "Requires Pre-Authorization is only allowed on insurance price list items.",
                        ENTITY,
                        "requiresPreAuthorization.insuranceOnly"
                );
            }

            return new ResolvedMapping(null, null, false);
        }

        BillingItemTypes billingItemType =
                BillingItemTypes.valueOf(row.itemType().name());

        Optional<WaseelItemMapping> mapping = Optional.empty();

        if (row.waseelItemMappingId() != null) {
            mapping = waseelItemMappingRepository.findById(
                    row.waseelItemMappingId()
            );
        }

        if (mapping.isEmpty()) {
            mapping = waseelItemMappingRepository
                    .findByItemTypeAndSourceIdAndIsActiveTrue(
                            billingItemType,
                            catalog.sourceId()
                    );
        }

        if (mapping.isEmpty()) {
            mapping = waseelItemMappingRepository
                    .findFirstByItemTypeAndItemCodeIgnoreCaseAndIsActiveTrue(
                            billingItemType,
                            catalog.itemCode()
                    );
        }

        WaseelItemMapping resolved = mapping.orElseThrow(() ->
                new BadRequestAlertException(
                        "Waseel item mapping was not found for "
                                + row.itemType()
                                + " / "
                                + catalog.itemCode()
                                + ".",
                        ENTITY,
                        "item.import.mappingRequired"
                )
        );

        Long sbsCatalogId = row.sbsCatalogId() != null
                ? row.sbsCatalogId()
                : resolved.getSbsCatalog() != null
                ? resolved.getSbsCatalog().getId()
                : null;

        if (sbsCatalogId == null) {
            throw new BadRequestAlertException(
                    "SBS catalog is required for insurance price list items.",
                    ENTITY,
                    "item.import.sbsRequired"
            );
        }

        return new ResolvedMapping(
                resolved.getId(),
                sbsCatalogId,
                Boolean.TRUE.equals(row.requiresPreAuthorization())
        );
    }

    private BadRequestAlertException catalogNotFound(ImportRow row) {
        return new BadRequestAlertException(
                "Catalog item was not found for "
                        + row.itemType()
                        + " / "
                        + row.itemCode()
                        + ". Use a valid item code or fill sourceId.",
                ENTITY,
                "item.import.catalogNotFound"
        );
    }

    private void writeHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        for (int index = 0; index < HEADERS.length; index++) {
            headerRow.createCell(index).setCellValue(HEADERS[index]);
        }
    }

    private void writeItemRow(Row row, PriceListSetupItem item) {
        row.createCell(0).setCellValue(
                item.getItemType() != null ? item.getItemType().name() : ""
        );
        row.createCell(1).setCellValue(
                item.getItemCode() != null ? item.getItemCode() : ""
        );
        row.createCell(2).setCellValue(
                item.getItemName() != null ? item.getItemName() : ""
        );
        if (item.getSourceId() != null) {
            row.createCell(3).setCellValue(item.getSourceId());
        }
        row.createCell(4).setCellValue(
                item.getCategory() != null ? item.getCategory() : ""
        );
        if (item.getUnitPrice() != null) {
            row.createCell(5).setCellValue(item.getUnitPrice().doubleValue());
        }
        if (item.getDiscountPercentage() != null) {
            row.createCell(6).setCellValue(
                    item.getDiscountPercentage().doubleValue()
            );
        }
        row.createCell(7).setCellValue(
                Boolean.TRUE.equals(item.getIsActive()) ? "TRUE" : "FALSE"
        );
        row.createCell(8).setCellValue(
                Boolean.TRUE.equals(item.getRequiresPreAuthorization())
                        ? "TRUE"
                        : "FALSE"
        );
        if (item.getWaseelItemMappingId() != null) {
            row.createCell(9).setCellValue(item.getWaseelItemMappingId());
        }
        if (item.getSbsCatalogId() != null) {
            row.createCell(10).setCellValue(item.getSbsCatalogId());
        }
    }

    private void writeInstructions(Sheet sheet) {
        String[] lines = {
                "How to use this template",
                "1. Keep the Items sheet header row as-is.",
                "2. Required columns: itemType, itemCode, unitPrice.",
                "3. itemType must be one of: SERVICE, PROCEDURE, MEDICATION, LABORATORY, RADIOLOGY, PATHOLOGY.",
                "4. itemCode must match the catalog code (service/procedure/medication/test).",
                "5. sourceId is optional. Fill it if you already know the catalog ID.",
                "6. discountPercentage is optional (default 0). isActive is optional (default TRUE).",
                "7. For insurance price lists, the system resolves Waseel mapping by item type + source/code.",
                "8. requiresPreAuthorization is only allowed for insurance price lists.",
                "9. Upload the filled .xlsx, .xls, or .csv file from Price List Items.",
                "10. Existing item codes on this price list are updated. New rows are added."
        };

        for (int index = 0; index < lines.length; index++) {
            sheet.createRow(index).createCell(0).setCellValue(lines[index]);
        }

        sheet.autoSizeColumn(0);
    }

    private void validateUploadedFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestAlertException(
                    "File is required.",
                    ENTITY,
                    "item.import.fileRequired"
            );
        }

        String name = file.getOriginalFilename() == null
                ? ""
                : file.getOriginalFilename().toLowerCase(Locale.ROOT);

        if (!name.endsWith(".csv")
                && !name.endsWith(".xlsx")
                && !name.endsWith(".xls")) {
            throw new BadRequestAlertException(
                    "Unsupported file type. Upload .csv, .xlsx, or .xls.",
                    ENTITY,
                    "item.import.badFileType"
            );
        }
    }

    private boolean isExcelFile(MultipartFile file) {
        String name = file.getOriginalFilename() == null
                ? ""
                : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        return name.endsWith(".xlsx") || name.endsWith(".xls");
    }

    private List<ImportRow> readCsv(MultipartFile file) {
        try (Reader reader = new InputStreamReader(
                file.getInputStream(),
                StandardCharsets.UTF_8
        );
             CSVParser parser = new CSVParser(
                     reader,
                     CSVFormat.DEFAULT
                             .withDelimiter(',')
                             .withFirstRecordAsHeader()
                             .withIgnoreHeaderCase()
                             .withTrim()
             )) {
            Map<String, String> canonicalHeaders =
                    canonicalizeHeaders(parser.getHeaderMap().keySet());
            ensureHeaders(canonicalHeaders.keySet());

            List<ImportRow> rows = new ArrayList<>();
            for (CSVRecord record : parser.getRecords()) {
                Map<String, String> values = new LinkedHashMap<>();
                for (Map.Entry<String, String> entry : canonicalHeaders.entrySet()) {
                    values.put(entry.getKey(), record.get(entry.getValue()));
                }

                if (isBlankRow(values)) {
                    continue;
                }

                rows.add(toImportRow(values, record.getRecordNumber()));
            }
            return rows;
        } catch (BadRequestAlertException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new BadRequestAlertException(
                    "Error reading CSV file: " + exception.getMessage(),
                    ENTITY,
                    "item.import.fileReadError"
            );
        }
    }

    private List<ImportRow> readExcel(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getNumberOfSheets() > 0
                    ? workbook.getSheetAt(0)
                    : null;

            if (sheet == null) {
                throw new BadRequestAlertException(
                        "Excel file has no sheets.",
                        ENTITY,
                        "item.import.emptySheet"
                );
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BadRequestAlertException(
                        "The first sheet must start with the template header row.",
                        ENTITY,
                        "item.import.missingHeader"
                );
            }

            Map<String, Integer> headerIndex = buildHeaderIndex(headerRow);
            ensureHeaders(headerIndex.keySet());

            List<ImportRow> rows = new ArrayList<>();
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlankExcelRow(row, headerIndex)) {
                    continue;
                }

                Map<String, String> values = readExcelRowValues(row, headerIndex);
                rows.add(toImportRow(values, rowIndex + 1L));
            }

            return rows;
        } catch (BadRequestAlertException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BadRequestAlertException(
                    "Error reading Excel file: " + exception.getMessage(),
                    ENTITY,
                    "item.import.fileReadError"
            );
        }
    }

    private Map<String, Integer> buildHeaderIndex(Row headerRow) {
        Map<String, Integer> headerIndex = new LinkedHashMap<>();
        short lastCell = headerRow.getLastCellNum();

        for (int index = 0; index < lastCell; index++) {
            Cell cell = headerRow.getCell(index);
            String raw = DATA_FORMATTER.formatCellValue(cell).trim();
            if (raw.isBlank()) {
                continue;
            }

            final int columnIndex = index;
            canonicalizeHeader(raw).ifPresent(canonical ->
                    headerIndex.putIfAbsent(canonical, columnIndex)
            );
        }

        return headerIndex;
    }

    private Map<String, String> canonicalizeHeaders(Set<String> headers) {
        Map<String, String> canonical = new LinkedHashMap<>();
        for (String header : headers) {
            canonicalizeHeader(header).ifPresent(key ->
                    canonical.putIfAbsent(key, header)
            );
        }
        return canonical;
    }

    private Optional<String> canonicalizeHeader(String header) {
        String normalized = normalizeHeader(header);
        for (Map.Entry<String, Set<String>> entry : HEADER_ALIASES.entrySet()) {
            if (entry.getValue().contains(normalized)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    private void ensureHeaders(Set<String> headers) {
        for (String required : REQUIRED_HEADERS) {
            if (!headers.contains(required)) {
                throw new BadRequestAlertException(
                        "Missing required column: " + required,
                        ENTITY,
                        "item.import.missingHeader"
                );
            }
        }
    }

    private Map<String, String> readExcelRowValues(
            Row row,
            Map<String, Integer> headerIndex
    ) {
        Map<String, String> values = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : headerIndex.entrySet()) {
            Cell cell = row.getCell(entry.getValue());
            values.put(
                    entry.getKey(),
                    DATA_FORMATTER.formatCellValue(cell).trim()
            );
        }
        return values;
    }

    private boolean isBlankExcelRow(
            Row row,
            Map<String, Integer> headerIndex
    ) {
        for (Integer index : headerIndex.values()) {
            String value = DATA_FORMATTER.formatCellValue(row.getCell(index))
                    .trim();
            if (!value.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private boolean isBlankRow(Map<String, String> values) {
        return values.values().stream()
                .allMatch(value -> value == null || value.isBlank());
    }

    private ImportRow toImportRow(Map<String, String> values, long rowNumber) {
        String itemTypeValue = value(values, "itemtype");
        String itemCode = value(values, "itemcode");
        String unitPriceValue = value(values, "unitprice");

        if (itemTypeValue == null || itemTypeValue.isBlank()) {
            throw new BadRequestAlertException(
                    "itemType is required.",
                    ENTITY,
                    "item.import.itemTypeRequired"
            );
        }

        if (itemCode == null || itemCode.isBlank()) {
            throw new BadRequestAlertException(
                    "itemCode is required.",
                    ENTITY,
                    "item.import.itemCodeRequired"
            );
        }

        PriceListItemType itemType;
        try {
            itemType = PriceListItemType.valueOf(
                    itemTypeValue.trim().toUpperCase(Locale.ROOT)
                            .replace(' ', '_')
                            .replace('-', '_')
            );
        } catch (IllegalArgumentException exception) {
            throw new BadRequestAlertException(
                    "Invalid itemType: "
                            + itemTypeValue
                            + ". Use SERVICE, PROCEDURE, MEDICATION, LABORATORY, RADIOLOGY, or PATHOLOGY.",
                    ENTITY,
                    "item.import.invalidItemType"
            );
        }

        BigDecimal unitPrice = parseDecimal(unitPriceValue, "unitPrice");
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestAlertException(
                    "unitPrice must be zero or greater.",
                    ENTITY,
                    "item.import.invalidUnitPrice"
            );
        }

        BigDecimal discount = parseDecimal(
                value(values, "discountpercentage"),
                "discountPercentage"
        );
        if (discount == null) {
            discount = BigDecimal.ZERO;
        }
        if (discount.compareTo(BigDecimal.ZERO) < 0
                || discount.compareTo(new BigDecimal("100")) > 0) {
            throw new BadRequestAlertException(
                    "discountPercentage must be between 0 and 100.",
                    ENTITY,
                    "item.import.invalidDiscount"
            );
        }

        return new ImportRow(
                Math.toIntExact(rowNumber),
                itemType,
                itemCode.trim(),
                blankToNull(value(values, "itemname")),
                parseLong(value(values, "sourceid"), "sourceId"),
                blankToNull(value(values, "category")),
                unitPrice,
                discount,
                parseBoolean(value(values, "isactive"), true),
                parseBoolean(value(values, "requirespreauthorization"), false),
                parseLong(value(values, "waseelitemmappingid"), "waseelItemMappingId"),
                parseLong(value(values, "sbscatalogid"), "sbsCatalogId")
        );
    }

    private String value(Map<String, String> values, String key) {
        return values.get(key);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        return second;
    }

    private String normalizeHeader(String header) {
        return header == null
                ? ""
                : header.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private BigDecimal parseDecimal(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(value.trim().replace(",", ""));
        } catch (NumberFormatException exception) {
            throw new BadRequestAlertException(
                    fieldName + " must be a number.",
                    ENTITY,
                    "item.import.invalidNumber"
            );
        }
    }

    private Long parseLong(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.valueOf(value.trim().replace(",", ""));
        } catch (NumberFormatException exception) {
            throw new BadRequestAlertException(
                    fieldName + " must be a whole number.",
                    ENTITY,
                    "item.import.invalidNumber"
            );
        }
    }

    private String exceptionMessage(Exception exception) {
        if (exception instanceof BadRequestAlertException badRequest
                && badRequest.getBody() != null
                && badRequest.getBody().getTitle() != null) {
            return badRequest.getBody().getTitle();
        }

        return exception.getMessage() != null
                ? exception.getMessage()
                : "Unable to import this row.";
    }

    private Boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (Set.of("true", "yes", "y", "1").contains(normalized)) {
            return true;
        }
        if (Set.of("false", "no", "n", "0").contains(normalized)) {
            return false;
        }

        throw new BadRequestAlertException(
                "Boolean columns must be TRUE/FALSE or YES/NO.",
                ENTITY,
                "item.import.invalidBoolean"
        );
    }

    private record ImportRow(
            Integer rowNumber,
            PriceListItemType itemType,
            String itemCode,
            String itemName,
            Long sourceId,
            String category,
            BigDecimal unitPrice,
            BigDecimal discountPercentage,
            Boolean isActive,
            Boolean requiresPreAuthorization,
            Long waseelItemMappingId,
            Long sbsCatalogId
    ) {
    }

    private record ResolvedCatalog(
            Long sourceId,
            String itemCode,
            String itemName
    ) {
    }

    private record ResolvedMapping(
            Long waseelItemMappingId,
            Long sbsCatalogId,
            Boolean requiresPreAuthorization
    ) {
    }
}
