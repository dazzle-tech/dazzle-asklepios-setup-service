package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.ICDCategory;
import com.dazzle.asklepios.domain.ICDDiagnosis;
import com.dazzle.asklepios.service.ICDTreeService;
import com.dazzle.asklepios.service.dto.icd10.ICDNodeDetailsEntityDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(ICDTreeController.class);

    private final ICDTreeService icdTreeService;

    public ICDTreeController(ICDTreeService icdTreeService) {
        this.icdTreeService = icdTreeService;
    }

    @GetMapping("/tree/root")
    public ResponseEntity<List<ICDCategory>> getRoot(
            @RequestParam String icdCoding,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST getRoot icdCoding='{}' pageable={}", icdCoding, pageable);

        if (icdCoding == null || icdCoding.isBlank()) {
            throw new BadRequestAlertException("icdCoding is required", "icdTree", "icdCoding.required");
        }
        if (pageable == null) {
            throw new BadRequestAlertException("pageable is required", "icdTree", "pageable.required");
        }

        Page<ICDCategory> page = icdTreeService.getRootCategories(icdCoding, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/tree/children")
    public ResponseEntity<List<ICDCategory>> getChildren(
            @RequestParam String icdCoding,
            @RequestParam String parentCategoryCode,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST getChildren icdCoding='{}' parentCategoryCode='{}' pageable={}",
                icdCoding, parentCategoryCode, pageable);

        if (icdCoding == null || icdCoding.isBlank()) {
            throw new BadRequestAlertException("icdCoding is required", "icdTree", "icdCoding.required");
        }
        if (parentCategoryCode == null || parentCategoryCode.isBlank()) {
            throw new BadRequestAlertException("parentCategoryCode is required", "icdTree", "parent.required");
        }
        if (pageable == null) {
            throw new BadRequestAlertException("pageable is required", "icdTree", "pageable.required");
        }

        Page<ICDCategory> page = icdTreeService.getChildren(icdCoding, parentCategoryCode, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/tree/node")
    public ResponseEntity<ICDNodeDetailsEntityDTO> getNodeDetails(
            @RequestParam String icdCoding,
            @RequestParam String categoryCode,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST getNodeDetails icdCoding='{}' categoryCode='{}' pageable={}",
                icdCoding, categoryCode, pageable);

        if (icdCoding == null || icdCoding.isBlank()) {
            throw new BadRequestAlertException("icdCoding is required", "icdTree", "icdCoding.required");
        }
        if (categoryCode == null || categoryCode.isBlank()) {
            throw new BadRequestAlertException("categoryCode is required", "icdTree", "category.required");
        }
        if (pageable == null) {
            throw new BadRequestAlertException("pageable is required", "icdTree", "pageable.required");
        }

        ICDNodeDetailsEntityDTO dto = icdTreeService.getNodeDetails(icdCoding, categoryCode, pageable);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/diagnoses")
    public ResponseEntity<List<ICDDiagnosis>> getDiagnosesByCategory(
            @RequestParam String icdCoding,
            @RequestParam String categoryCode,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST getDiagnosesByCategory icdCoding='{}' categoryCode='{}' pageable={}",
                icdCoding, categoryCode, pageable);

        if (icdCoding == null || icdCoding.isBlank()) {
            throw new BadRequestAlertException("icdCoding is required", "icdTree", "icdCoding.required");
        }
        if (categoryCode == null || categoryCode.isBlank()) {
            throw new BadRequestAlertException("categoryCode is required", "icdTree", "category.required");
        }
        if (pageable == null) {
            throw new BadRequestAlertException("pageable is required", "icdTree", "pageable.required");
        }

        Page<ICDDiagnosis> page = icdTreeService.getDiagnosesByCategory(icdCoding, categoryCode, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/diagnoses/search")
    public ResponseEntity<List<ICDDiagnosis>> searchDiagnoses(
            @RequestParam String keyword,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST searchDiagnoses keyword='{}' pageable={}", keyword, pageable);

        if (keyword == null || keyword.isBlank()) {
            throw new BadRequestAlertException(
                    "keyword is required",
                    "icdTree",
                    "keyword.required"
            );
        }

        if (keyword.trim().length() < 3) {
            throw new BadRequestAlertException(
                    "keyword must be at least 3 characters long",
                    "icdTree",
                    "keyword.tooShort"
            );
        }

        if (pageable == null) {
            throw new BadRequestAlertException(
                    "pageable is required",
                    "icdTree",
                    "pageable.required"
            );
        }

        Page<ICDDiagnosis> page = icdTreeService.searchDiagnoses(keyword, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}
