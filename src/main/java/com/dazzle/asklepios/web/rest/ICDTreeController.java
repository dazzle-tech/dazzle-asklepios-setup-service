package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.ICDCategory;
import com.dazzle.asklepios.domain.ICDDiagnosis;
import com.dazzle.asklepios.service.ICDTreeService;
import com.dazzle.asklepios.service.dto.icd10.ICDNodeDetailsEntityDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
@Validated
public class ICDTreeController {

    private static final Logger LOG = LoggerFactory.getLogger(ICDTreeController.class);

    private final ICDTreeService icdTreeService;

    public ICDTreeController(ICDTreeService icdTreeService) {
        this.icdTreeService = icdTreeService;
    }

    @GetMapping("/icd/tree/root")
    public ResponseEntity<List<ICDCategory>> getRoot(
            @RequestParam @NotBlank String icdCoding,
            @ParameterObject @NotNull Pageable pageable
    ) {
        LOG.debug("REST getRoot icdCoding='{}' pageable={}", icdCoding, pageable);

        Page<ICDCategory> page = icdTreeService.getRootCategories(icdCoding, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/icd/tree/children")
    public ResponseEntity<List<ICDCategory>> getChildren(
            @RequestParam @NotBlank String icdCoding,
            @RequestParam @NotBlank String parentCategoryCode,
            @ParameterObject @NotNull Pageable pageable
    ) {
        LOG.debug("REST getChildren icdCoding='{}' parentCategoryCode='{}' pageable={}",
                icdCoding, parentCategoryCode, pageable);

        Page<ICDCategory> page = icdTreeService.getChildren(icdCoding, parentCategoryCode, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/icd/tree/node")
    public ResponseEntity<ICDNodeDetailsEntityDTO> getNodeDetails(
            @RequestParam @NotBlank String icdCoding,
            @RequestParam @NotBlank String categoryCode,
            @ParameterObject @NotNull Pageable pageable
    ) {
        LOG.debug("REST getNodeDetails icdCoding='{}' categoryCode='{}' pageable={}",
                icdCoding, categoryCode, pageable);

        ICDNodeDetailsEntityDTO dto = icdTreeService.getNodeDetails(icdCoding, categoryCode, pageable);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/icd/diagnoses")
    public ResponseEntity<List<ICDDiagnosis>> getDiagnosesByCategory(
            @RequestParam @NotBlank String icdCoding,
            @RequestParam @NotBlank String categoryCode,
            @ParameterObject @NotNull Pageable pageable
    ) {
        LOG.debug("REST getDiagnosesByCategory icdCoding='{}' categoryCode='{}' pageable={}",
                icdCoding, categoryCode, pageable);

        Page<ICDDiagnosis> page = icdTreeService.getDiagnosesByCategory(icdCoding, categoryCode, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/icd/diagnoses/search")
    public ResponseEntity<List<ICDDiagnosis>> searchDiagnoses(
            @RequestParam @NotBlank @Size(min = 3) String keyword,
            @ParameterObject @NotNull Pageable pageable
    ) {
        LOG.debug("REST searchDiagnoses keyword='{}' pageable={}", keyword, pageable);

        Page<ICDDiagnosis> page = icdTreeService.searchDiagnoses(keyword.trim(), pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/icd/diagnoses/by-ids")
    public ResponseEntity<List<ICDDiagnosis>> getDiagnosesByIds(
            @RequestParam @NotNull @Size(min = 1) List<Long> ids
    ) {
        LOG.debug("REST getDiagnosesByIds ids={}", ids);
        return ResponseEntity.ok(icdTreeService.findByIds(ids));
    }

    @GetMapping("/icd/diagnoses/{id}")
    public ResponseEntity<ICDDiagnosis> getDiagnosisById(
            @PathVariable @NotNull Long id
    ) {
        LOG.debug("REST getDiagnosisById id={}", id);
        ICDDiagnosis diagnosis = icdTreeService.getDiagnosisById(id);
        return ResponseEntity.ok(diagnosis);
    }
}
