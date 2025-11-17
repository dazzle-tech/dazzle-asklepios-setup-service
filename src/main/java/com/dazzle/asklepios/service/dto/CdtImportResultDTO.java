package com.dazzle.asklepios.service.dto;
import java.util.List;

public record CdtImportResultDTO(
        int totalRows,
        int inserted,
        int updated,
        List<CdtConflictDTO> conflicts
) {}