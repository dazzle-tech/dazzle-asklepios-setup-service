package com.dazzle.asklepios.service.dto;

public record PriceListSetupItemImportErrorDTO(
        Integer rowNumber,
        String itemCode,
        String message
) {
}
