package com.dazzle.asklepios.service.dto;

import java.util.List;

public record LoincImportResultDTO(
        Integer totalRows,
        Integer inserted,
        Integer updated,
        List<LoincConflictDTO> conflicts
) {}
