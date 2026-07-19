package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.PriceListSetupItemService;
import com.dazzle.asklepios.service.dto.PriceListSetupItemDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class PriceListSetupItemController {

    private final PriceListSetupItemService
            priceListSetupItemService;

    @PostMapping("/price-list-setups/{priceListSetupId}/items")
    public ResponseEntity<PriceListSetupItemDTO>
    createPriceListSetupItem(
            @PathVariable Long priceListSetupId,
            @Valid @RequestBody PriceListSetupItemDTO dto
    ) {
        PriceListSetupItemDTO result =
                priceListSetupItemService.create(
                        priceListSetupId,
                        dto
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }

    @PutMapping("/price-list-setups/{priceListSetupId}/items/{itemId}")
    public ResponseEntity<PriceListSetupItemDTO>
    updatePriceListSetupItem(
            @PathVariable Long priceListSetupId,
            @PathVariable Long itemId,
            @Valid @RequestBody PriceListSetupItemDTO dto
    ) {
        PriceListSetupItemDTO result =
                priceListSetupItemService.update(
                        priceListSetupId,
                        itemId,
                        dto
                );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/price-list-setups/{priceListSetupId}/items/{itemId}")
    public ResponseEntity<PriceListSetupItemDTO>
    getPriceListSetupItem(
            @PathVariable Long priceListSetupId,
            @PathVariable Long itemId
    ) {
        PriceListSetupItemDTO result =
                priceListSetupItemService.findById(
                        priceListSetupId,
                        itemId
                );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/price-list-setups/{priceListSetupId}/items")
    public ResponseEntity<Page<PriceListSetupItemDTO>>
    getAllPriceListSetupItems(
            @PathVariable Long priceListSetupId,
            Pageable pageable
    ) {
        Page<PriceListSetupItemDTO> result =
                priceListSetupItemService.findAll(
                        priceListSetupId,
                        pageable
                );

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/price-list-setups/{priceListSetupId}/items/{itemId}")
    public ResponseEntity<Void> deletePriceListSetupItem(
            @PathVariable Long priceListSetupId,
            @PathVariable Long itemId
    ) {
        priceListSetupItemService.delete(
                priceListSetupId,
                itemId
        );

        return ResponseEntity.noContent().build();
    }
}