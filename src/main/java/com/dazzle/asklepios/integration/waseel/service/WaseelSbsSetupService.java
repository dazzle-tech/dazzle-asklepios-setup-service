package com.dazzle.asklepios.integration.waseel.service;

import com.dazzle.asklepios.domain.WaseelItemMapping;
import com.dazzle.asklepios.domain.WaseelSbsCatalog;
import com.dazzle.asklepios.domain.WaseelSbsImportLog;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.integration.waseel.dto.WaseelItemMappingDTO;
import com.dazzle.asklepios.integration.waseel.dto.WaseelItemMappingRequest;
import com.dazzle.asklepios.integration.waseel.dto.WaseelSbsCatalogDTO;
import com.dazzle.asklepios.integration.waseel.dto.WaseelSbsImportResultDTO;
import com.dazzle.asklepios.repository.WaseelItemMappingRepository;
import com.dazzle.asklepios.repository.WaseelSbsCatalogRepository;
import com.dazzle.asklepios.repository.WaseelSbsImportLogRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Optional;

@Service
@Transactional
public class WaseelSbsSetupService {

    private final WaseelSbsCatalogRepository sbsCatalogRepository;
    private final WaseelItemMappingRepository itemMappingRepository;
    private final WaseelSbsImportLogRepository importLogRepository;

    public WaseelSbsSetupService(
            WaseelSbsCatalogRepository sbsCatalogRepository,
            WaseelItemMappingRepository itemMappingRepository,
            WaseelSbsImportLogRepository importLogRepository
    ) {
        this.sbsCatalogRepository = sbsCatalogRepository;
        this.itemMappingRepository = itemMappingRepository;
        this.importLogRepository = importLogRepository;
    }

    public WaseelSbsImportResultDTO importSbsExcel(MultipartFile file) {

        long totalRows = 0L;
        long successRows = 0L;
        long failedRows = 0L;
        StringBuilder errors = new StringBuilder();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                totalRows++;

                try {

                    Row row = sheet.getRow(i);

                    if (row == null) {
                        failedRows++;
                        continue;
                    }

                    String waseelItemType = normalizeWaseelItemType(getCellValue(row.getCell(0)));
                    String sbsCode = getCellValue(row.getCell(1));
                    String updateType = getCellValue(row.getCell(2));
                    String revisionDetails = getCellValue(row.getCell(3));
                    String shortDescription = getCellValue(row.getCell(4));
                    String longDescription = getCellValue(row.getCell(5));

                    if (sbsCode == null || sbsCode.isBlank()) {
                        failedRows++;
                        errors.append("Row ")
                                .append(i + 1)
                                .append(": SBS code is empty\n");
                        continue;
                    }

                    if (waseelItemType == null || waseelItemType.isBlank()) {
                        failedRows++;
                        errors.append("Row ")
                                .append(i + 1)
                                .append(": Waseel item type is empty\n");
                        continue;
                    }

                    WaseelSbsCatalog catalog = sbsCatalogRepository
                            .findBySbsCode(sbsCode)
                            .orElseGet(WaseelSbsCatalog::new);

                    catalog.setWaseelItemType(waseelItemType);
                    catalog.setSbsCode(sbsCode);
                    catalog.setUpdateType(updateType);
                    catalog.setRevisionDetails(revisionDetails);
                    catalog.setShortDescription(shortDescription);
                    catalog.setLongDescription(longDescription);
                    catalog.setIsActive(true);
                    catalog.setSourceFileName(file.getOriginalFilename());

                    if (catalog.getId() == null) {
                        catalog.setCreatedBy("system");
                        catalog.setCreatedDate(Instant.now());
                    } else {
                        catalog.setLastModifiedBy("system");
                        catalog.setLastModifiedDate(Instant.now());
                    }

                    sbsCatalogRepository.save(catalog);
                    successRows++;

                } catch (Exception ex) {

                    failedRows++;

                    errors.append("Row ")
                            .append(i + 1)
                            .append(": ")
                            .append(ex.getMessage())
                            .append("\n");
                }
            }

            saveImportLog(
                    file.getOriginalFilename(),
                    totalRows,
                    successRows,
                    failedRows,
                    errors.toString()
            );

            return new WaseelSbsImportResultDTO(
                    totalRows,
                    successRows,
                    failedRows,
                    "SBS file imported successfully",
                    errors.toString()
            );

        } catch (Exception ex) {

            saveImportLog(
                    file.getOriginalFilename(),
                    totalRows,
                    successRows,
                    failedRows,
                    ex.getMessage()
            );

            return new WaseelSbsImportResultDTO(
                    totalRows,
                    successRows,
                    failedRows,
                    "Failed to import SBS file",
                    ex.getMessage()
            );
        }
    }
    private String normalizeWaseelItemType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase();
    }

    @Transactional(readOnly = true)
    public Page<WaseelSbsCatalogDTO> searchSbs(String search, Pageable pageable) {
        Page<WaseelSbsCatalog> page;

        if (search == null || search.isBlank()) {
            page = sbsCatalogRepository.findAll(pageable);
        } else {
            page = sbsCatalogRepository
                    .findBySbsCodeContainingIgnoreCaseOrShortDescriptionContainingIgnoreCaseOrLongDescriptionContainingIgnoreCase(
                            search,
                            search,
                            search,
                            pageable
                    );
        }

        return page.map(this::toSbsDto);
    }

    @Transactional(readOnly = true)
    public WaseelSbsCatalogDTO getSbsById(Long id) {
        WaseelSbsCatalog catalog = sbsCatalogRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("SBS code not found", "WaseelSbsCatalog", "sbsCodeNotFound"));

        return toSbsDto(catalog);
    }

    public WaseelItemMappingDTO createMapping(WaseelItemMappingRequest request) {
        if (request.itemType() == null) {
            throw new BadRequestAlertException("Item type is required", "WaseelItemMapping", "itemTypeRequired");
        }

        if (request.sourceId() == null) {
            throw new BadRequestAlertException("Selected item is required" , "WaseelItemMapping", "itemRequired");
        }

        WaseelSbsCatalog sbsCatalog = sbsCatalogRepository.findById(request.sbsCatalogId())
                .orElseThrow(() -> new BadRequestAlertException("SBS code not found", "WaseelSbsCatalog", "sbsCodeNotFound"));

        if (sbsCatalog.getWaseelItemType() == null || sbsCatalog.getWaseelItemType().isBlank()) {
            throw new RuntimeException("Selected SBS code does not have Waseel item type");
        }

        WaseelItemMapping mapping = new WaseelItemMapping();
        mapping.setItemType(request.itemType());
        mapping.setSourceId(request.sourceId());
        mapping.setItemCode(request.itemCode());
        mapping.setItemName(request.itemName());
        mapping.setSbsCatalog(sbsCatalog);
        mapping.setRequiresPreAuthorization(Boolean.TRUE.equals(request.requiresPreauth()));
        mapping.setIsActive(request.isActive() == null || request.isActive());
        mapping.setNotes(request.notes());
        mapping.setCreatedBy("system");
        mapping.setCreatedDate(Instant.now());

        return toMappingDto(itemMappingRepository.save(mapping));
    }
    public WaseelItemMappingDTO updateMapping(Long id, WaseelItemMappingRequest request) {
        WaseelItemMapping mapping = itemMappingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mapping not found"));

        if (request.sbsCatalogId() != null) {
            WaseelSbsCatalog sbsCatalog = sbsCatalogRepository.findById(request.sbsCatalogId())
                    .orElseThrow(() -> new RuntimeException("SBS code not found"));

            mapping.setSbsCatalog(sbsCatalog);
        }


        mapping.setRequiresPreAuthorization(Boolean.TRUE.equals(request.requiresPreauth()));
        mapping.setIsActive(request.isActive() == null || request.isActive());
        mapping.setNotes(request.notes());
        mapping.setLastModifiedBy("system");
        mapping.setLastModifiedDate(Instant.now());

        return toMappingDto(itemMappingRepository.save(mapping));
    }

    @Transactional(readOnly = true)
    public Page<WaseelItemMappingDTO> searchMappings(Pageable pageable) {
        return itemMappingRepository.findAll(pageable).map(this::toMappingDto);
    }

    public void deactivateMapping(Long id) {
        WaseelItemMapping mapping = itemMappingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mapping not found"));

        mapping.setIsActive(false);
        mapping.setLastModifiedBy("system");
        mapping.setLastModifiedDate(Instant.now());

        itemMappingRepository.save(mapping);
    }

    private WaseelSbsCatalogDTO toSbsDto(WaseelSbsCatalog catalog) {
        return new WaseelSbsCatalogDTO(
                catalog.getId(),
                catalog.getWaseelItemType(),
                catalog.getSbsCode(),
                catalog.getUpdateType(),
                catalog.getRevisionDetails(),
                catalog.getShortDescription(),
                catalog.getLongDescription(),
                catalog.getIsActive()
        );
    }

    private WaseelItemMappingDTO toMappingDto(WaseelItemMapping mapping) {
        WaseelSbsCatalog catalog = mapping.getSbsCatalog();

        return new WaseelItemMappingDTO(
                mapping.getId(),
                mapping.getItemType(),
                mapping.getSourceId(),
                mapping.getItemCode(),
                mapping.getItemName(),
                catalog.getId(),
                catalog.getWaseelItemType(),
                catalog.getSbsCode(),
                catalog.getShortDescription(),
                mapping.getRequiresPreAuthorization(),
                mapping.getIsActive(),
                mapping.getNotes()
        );
    }

    private void saveImportLog(
            String fileName,
            Long totalRows,
            Long successRows,
            Long failedRows,
            String errorDetails
    ) {
        WaseelSbsImportLog log = new WaseelSbsImportLog();
        log.setFileName(fileName);
        log.setTotalRows(totalRows);
        log.setSuccessRows(successRows);
        log.setFailedRows(failedRows);
        log.setErrorDetails(errorDetails);

        importLogRepository.save(log);
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        DataFormatter formatter = new DataFormatter();
        String value = formatter.formatCellValue(cell);

        return value != null ? value.trim() : null;
    }

    @Transactional(readOnly = true)
    public Optional<WaseelItemMappingDTO> getMappingByItem(BillingItemTypes itemType, Long sourceId) {
        return itemMappingRepository
                .findByItemTypeAndSourceIdAndIsActiveTrue(itemType, sourceId)
                .map(this::toMappingDto);
    }

    @Transactional(readOnly = true)
    public boolean requiresPreauth(BillingItemTypes itemType, Long sourceId) {

        return itemMappingRepository
                .findByItemTypeAndSourceIdAndIsActiveTrue(itemType, sourceId)
                .map(WaseelItemMapping::getRequiresPreAuthorization)
                .orElse(false);
    }
}