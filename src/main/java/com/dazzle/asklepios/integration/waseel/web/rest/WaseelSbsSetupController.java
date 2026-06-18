package com.dazzle.asklepios.integration.waseel.web.rest;

import com.dazzle.asklepios.integration.waseel.dto.WaseelItemMappingDTO;
import com.dazzle.asklepios.integration.waseel.dto.WaseelItemMappingRequest;
import com.dazzle.asklepios.integration.waseel.dto.WaseelSbsCatalogDTO;
import com.dazzle.asklepios.integration.waseel.dto.WaseelSbsImportResultDTO;
import com.dazzle.asklepios.integration.waseel.service.WaseelSbsSetupService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/setup")
public class WaseelSbsSetupController {

    private final WaseelSbsSetupService waseelSbsSetupService;

    public WaseelSbsSetupController(WaseelSbsSetupService waseelSbsSetupService) {
        this.waseelSbsSetupService = waseelSbsSetupService;
    }

    @PostMapping("/waseel/sbs/import")
    public ResponseEntity<WaseelSbsImportResultDTO> importSbsExcel(
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(waseelSbsSetupService.importSbsExcel(file));
    }

    @GetMapping("/waseel/sbs")
    public ResponseEntity<Page<WaseelSbsCatalogDTO>> searchSbs(
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return ResponseEntity.ok(waseelSbsSetupService.searchSbs(search, pageable));
    }

    @GetMapping("/waseel/sbs/{id}")
    public ResponseEntity<WaseelSbsCatalogDTO> getSbsById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(waseelSbsSetupService.getSbsById(id));
    }

    @PostMapping("/waseel/item-mapping")
    public ResponseEntity<WaseelItemMappingDTO> createMapping(
            @Valid @RequestBody WaseelItemMappingRequest request
    ) {
        return ResponseEntity.ok(waseelSbsSetupService.createMapping(request));
    }

    @PutMapping("/waseel/item-mapping/{id}")
    public ResponseEntity<WaseelItemMappingDTO> updateMapping(
            @PathVariable Long id,
            @Valid @RequestBody WaseelItemMappingRequest request
    ) {
        return ResponseEntity.ok(waseelSbsSetupService.updateMapping(id, request));
    }

    @GetMapping("/waseel/item-mapping")
    public ResponseEntity<Page<WaseelItemMappingDTO>> searchMappings(
            Pageable pageable
    ) {
        return ResponseEntity.ok(waseelSbsSetupService.searchMappings(pageable));
    }
    

    @DeleteMapping("/waseel/item-mapping/{id}")
    public ResponseEntity<Void> deactivateMapping(
            @PathVariable Long id
    ) {
        waseelSbsSetupService.deactivateMapping(id);
        return ResponseEntity.noContent().build();
    }
}