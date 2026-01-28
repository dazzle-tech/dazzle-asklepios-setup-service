package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.ICDTreeService;
import com.dazzle.asklepios.service.dto.icd10.ICDCategoryDTO;
import com.dazzle.asklepios.service.dto.icd10.ICDDiagnosisDTO;
import com.dazzle.asklepios.service.dto.icd10.ICDNodeDetailsDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/setup/icd")
public class ICDTreeController {

    private final ICDTreeService icdTreeService;

    public ICDTreeController(ICDTreeService icdTreeService) {
        this.icdTreeService = icdTreeService;
    }

    @GetMapping("/tree/root")
    public ResponseEntity<List<ICDCategoryDTO>> getRoot(
            @RequestParam String icdCoding,
            @ParameterObject Pageable pageable
    ) {

        if (icdCoding == null || icdCoding.isBlank()) {
            throw new BadRequestAlertException("icdCoding is required", "icdTree", "icdCoding.required");
        }
        if (pageable == null) {
            throw new BadRequestAlertException("pageable is required", "icdTree", "pageable.required");
        }

        Page<ICDCategoryDTO> page = icdTreeService.getRootCategories(icdCoding, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/tree/children")
    public ResponseEntity<List<ICDCategoryDTO>> getChildren(
            @RequestParam String icdCoding,
            @RequestParam String parentCategoryCode,
            @ParameterObject Pageable pageable
    ) {
        if (icdCoding == null || icdCoding.isBlank()) {
            throw new BadRequestAlertException("icdCoding is required", "icdTree", "icdCoding.required");
        }
        if (parentCategoryCode == null || parentCategoryCode.isBlank()) {
            throw new BadRequestAlertException("parentCategoryCode is required", "icdTree", "parent.required");
        }
        if (pageable == null) {
            throw new BadRequestAlertException("pageable is required", "icdTree", "pageable.required");
        }

        Page<ICDCategoryDTO> page = icdTreeService.getChildren(icdCoding, parentCategoryCode, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/tree/node")
    public ResponseEntity<ICDNodeDetailsDTO> getNodeDetails(
            @RequestParam String icdCoding,
            @RequestParam String categoryCode,
            @ParameterObject Pageable pageable
    ) {
        if (icdCoding == null || icdCoding.isBlank()) {
            throw new BadRequestAlertException("icdCoding is required", "icdTree", "icdCoding.required");
        }
        if (categoryCode == null || categoryCode.isBlank()) {
            throw new BadRequestAlertException("categoryCode is required", "icdTree", "category.required");
        }
        if (pageable == null) {
            throw new BadRequestAlertException("pageable is required", "icdTree", "pageable.required");
        }

        ICDNodeDetailsDTO dto = icdTreeService.getNodeDetails(icdCoding, categoryCode, pageable);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/diagnoses")
    public ResponseEntity<List<ICDDiagnosisDTO>> getDiagnosesByCategory(
            @RequestParam String icdCoding,
            @RequestParam String categoryCode,
            @ParameterObject Pageable pageable
    ) {
        if (icdCoding == null || icdCoding.isBlank()) {
            throw new BadRequestAlertException("icdCoding is required", "icdTree", "icdCoding.required");
        }
        if (categoryCode == null || categoryCode.isBlank()) {
            throw new BadRequestAlertException("categoryCode is required", "icdTree", "category.required");
        }
        if (pageable == null) {
            throw new BadRequestAlertException("pageable is required", "icdTree", "pageable.required");
        }

        Page<ICDDiagnosisDTO> page = icdTreeService.getDiagnosesByCategory(icdCoding, categoryCode, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
