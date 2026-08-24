package com.dazzle.asklepios.service.dto;

import java.util.List;

public record PriceListSetupItemImportResultDTO(
        Integer totalRows,
        Integer inserted,
        Integer updated,
        Integer failed,
        List<PriceListSetupItemImportErrorDTO> errors
) {
}
