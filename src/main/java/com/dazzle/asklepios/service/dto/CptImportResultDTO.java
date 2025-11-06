package com.dazzle.asklepios.service.dto;

import java.util.List;

public record CptImportResultDTO(
        Integer totalRows,
        Integer inserted,
        Integer updated,
        List<CptConflictDTO> conflicts
) {}
