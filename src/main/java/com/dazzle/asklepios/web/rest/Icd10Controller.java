package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Icd10Code;
import com.dazzle.asklepios.service.Icd10Service;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.department.DepartmentResponseVM;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/setup/icd10")
@RequiredArgsConstructor
public class Icd10Controller {
    private static final Logger LOG = LoggerFactory.getLogger(Icd10Controller.class);
    private final Icd10Service icd10Service;

    @PostMapping("/import")
    public ResponseEntity<String> importIcd10(@RequestParam("file") MultipartFile file) {
        icd10Service.importCsv(file);
        return ResponseEntity.ok("ICD-10 import completed successfully");
    }


    @GetMapping("/all")
    public ResponseEntity<List<Icd10Code>> getAllIcd10(@ParameterObject Pageable pageable) {
        LOG.debug("REST list ICD_10 page={}", pageable);
        final Page<Icd10Code> list = icd10Service.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), list
        );
        return new ResponseEntity<>(list.getContent(), headers, HttpStatus.OK);
    }




}

