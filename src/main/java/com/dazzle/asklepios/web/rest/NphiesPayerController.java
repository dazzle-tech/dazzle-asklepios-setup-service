package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.service.NphiesPayerService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class NphiesPayerController {

    private static final Logger LOG =
            LoggerFactory.getLogger(NphiesPayerController.class);

    private final NphiesPayerService nphiesPayerService;

    public NphiesPayerController(
            NphiesPayerService nphiesPayerService
    ) {
        this.nphiesPayerService = nphiesPayerService;
    }

    @GetMapping("/nphies-payers")
    public ResponseEntity<List<NphiesPayer>> getAllNphiesPayers(
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST list ALL NPHIES Payers pageable={}", pageable);

        Page<NphiesPayer> page = nphiesPayerService.findAll(pageable);

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

    @GetMapping("/nphies-payers/by-nphies-id/{nphiesId}")
    public ResponseEntity<List<NphiesPayer>> getByNphiesId(
            @PathVariable String nphiesId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug(
                "REST list NPHIES Payers by nphiesId='{}' pageable={}",
                nphiesId,
                pageable
        );

        Page<NphiesPayer> page = nphiesPayerService.findByNphiesId(nphiesId, pageable);

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

    @GetMapping("/nphies-payers/by-name-en/{nameEn}")
    public ResponseEntity<List<NphiesPayer>> getByNameEn(
            @PathVariable String nameEn,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug(
                "REST list NPHIES Payers by nameEn='{}' pageable={}",
                nameEn,
                pageable
        );

        Page<NphiesPayer> page = nphiesPayerService.findByNameEn(nameEn, pageable);

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

    @GetMapping("/nphies-payers/by-name-ar/{nameAr}")
    public ResponseEntity<List<NphiesPayer>> getByNameAr(
            @PathVariable String nameAr,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug(
                "REST list NPHIES Payers by nameAr='{}' pageable={}",
                nameAr,
                pageable
        );

        Page<NphiesPayer> page = nphiesPayerService.findByNameAr(nameAr, pageable);

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

    @GetMapping("/nphies-payers/{id}")
    public ResponseEntity<NphiesPayer> getById(@PathVariable Long id) {
        LOG.debug("REST get NPHIES Payer id={}", id);

        return nphiesPayerService.findOne(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
