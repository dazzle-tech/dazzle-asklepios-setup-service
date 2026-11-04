package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.service.PriceListSetupItemService;
import com.dazzle.asklepios.service.dto.PriceListItemWaseelCodesDTO;
import com.dazzle.asklepios.service.dto.PriceListSetupItemDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class PriceListSetupItemController {

    private static final Logger LOG =
            LoggerFactory.getLogger(PriceListSetupItemController.class);

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
    public ResponseEntity<List<PriceListSetupItemDTO>>
    getAllPriceListSetupItems(
            @PathVariable Long priceListSetupId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PriceListItemType itemType,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug(
                "REST search PriceListSetupItems priceListSetupId={} search={} itemType={} pageable={}",
                priceListSetupId,
                search,
                itemType,
                pageable
        );

        Page<PriceListSetupItemDTO> page =
                priceListSetupItemService.search(
                        priceListSetupId,
                        search,
                        itemType,
                        pageable
                );

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(
                page.getContent(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/price-list-setup-items/waseel-codes")
    public ResponseEntity<PriceListItemWaseelCodesDTO> getInsuranceWaseelCodes(
            @RequestParam PriceListItemType itemType,
            @RequestParam Long sourceId,
            @RequestParam(required = false) Long facilityId
    ) {
        return ResponseEntity.ok(
                priceListSetupItemService
                        .findInsuranceWaseelCodes(itemType, sourceId, facilityId)
                        .orElse(new PriceListItemWaseelCodesDTO(null, null))
        );
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