package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Icd10Code;
import com.dazzle.asklepios.service.Icd10Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/setup/icd10")
@RequiredArgsConstructor
public class Icd10Controller {

    private final Icd10Service icd10Service;

    @PostMapping("/import")
    public ResponseEntity<String> importIcd10(@RequestParam("file") MultipartFile file) {
        icd10Service.importCsv(file);
        return ResponseEntity.ok("ICD-10 import completed successfully");
    }


    @GetMapping("/all")
    public ResponseEntity<List<Icd10Code>> getAllIcd10() {
        List<Icd10Code> list = icd10Service.findAll();
        return ResponseEntity.ok(list);
    }



}

