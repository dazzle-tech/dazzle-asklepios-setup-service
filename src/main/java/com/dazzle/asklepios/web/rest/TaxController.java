package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Tax;
import com.dazzle.asklepios.domain.enumeration.TaxCalculationType;
import com.dazzle.asklepios.domain.enumeration.TaxType;
import com.dazzle.asklepios.service.TaxService;
import com.dazzle.asklepios.service.dto.TaxDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class TaxController {

    private static final Logger LOG =
            LoggerFactory.getLogger(TaxController.class);

    private static final String ENTITY_NAME = "tax";

    private final TaxService taxService;

    public TaxController(TaxService taxService) {
        this.taxService = taxService;
    }

    @PostMapping("/tax")
    public ResponseEntity<Tax> create(
            @Valid
            @RequestBody
            @NotNull
            TaxDTO taxDTO
    ) {
        LOG.debug(
                "REST request to create Tax payload={}",
                taxDTO
        );

        if (taxDTO.id() != null) {
            throw new BadRequestAlertException(
                    "A new tax cannot already have an id",
                    ENTITY_NAME,
                    "id.exists"
            );
        }

        Tax created = taxService.create(taxDTO);

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/setup/tax/"
                                        + created.getId()
                        )
                )
                .body(created);
    }

    @PutMapping("/tax/{id}")
    public ResponseEntity<Tax> update(
            @PathVariable
            @NotNull
            Long id,

            @Valid
            @RequestBody
            @NotNull
            TaxDTO taxDTO
    ) {
        LOG.debug(
                "REST request to update Tax id={} payload={}",
                id,
                taxDTO
        );

        if (taxDTO.id() == null) {
            throw new BadRequestAlertException(
                    "Tax id is required",
                    ENTITY_NAME,
                    "id.required"
            );
        }

        if (!id.equals(taxDTO.id())) {
            throw new BadRequestAlertException(
                    "Path id and payload id do not match",
                    ENTITY_NAME,
                    "id.mismatch"
            );
        }

        return ResponseEntity.ok(
                taxService.update(id, taxDTO)
        );
    }

    @PatchMapping("/tax/{id}/toggle-active")
    public ResponseEntity<Tax> toggleActive(
            @PathVariable
            @NotNull
            Long id
    ) {
        return ResponseEntity.ok(
                taxService.toggleActive(id)
        );
    }

    @PutMapping("/tax/{id}/activation-status/{active}")
    public ResponseEntity<Tax> changeActivationStatus(
            @PathVariable
            @NotNull
            Long id,

            @PathVariable
            @NotNull
            Boolean active
    ) {
        return ResponseEntity.ok(
                taxService.changeActivationStatus(
                        id,
                        active
                )
        );
    }

    @PatchMapping("/tax/{id}/set-default")
    public ResponseEntity<Tax> setDefault(
            @PathVariable
            @NotNull
            Long id
    ) {
        return ResponseEntity.ok(
                taxService.setDefault(id)
        );
    }

    @GetMapping("/tax/{id}")
    public ResponseEntity<Tax> findById(
            @PathVariable
            @NotNull
            Long id
    ) {
        return ResponseEntity.ok(
                taxService.findById(id)
        );
    }

    @GetMapping("/tax")
    public ResponseEntity<List<Tax>> findAll(
            @NotNull
            Long facilityId,

            @ParameterObject
            Pageable pageable
    ) {
        Page<Tax> page =
                taxService.findAllByFacilityId(
                        facilityId,
                        pageable
                );

        return pagedResponse(page);
    }

    @GetMapping("/tax/active")
    public ResponseEntity<List<Tax>> findAllActive(
            @NotNull
            Long facilityId,

            @ParameterObject
            Pageable pageable
    ) {
        Page<Tax> page =
                taxService.findAllActiveByFacilityId(
                        facilityId,
                        pageable
                );

        return pagedResponse(page);
    }

    @GetMapping("/tax/default/{facilityId}")
    public ResponseEntity<Tax> findDefaultTax(
            @PathVariable
            @NotNull
            Long facilityId
    ) {
        return ResponseEntity.ok(
                taxService.findDefaultTax(facilityId)
        );
    }

    @GetMapping("/tax/by-code/{code}")
    public ResponseEntity<List<Tax>> findByCode(
            @NotNull
            Long facilityId,

            @PathVariable
            String code,

            @ParameterObject
            Pageable pageable
    ) {
        return pagedResponse(
                taxService.findByCode(
                        facilityId,
                        code,
                        pageable
                )
        );
    }

    @GetMapping("/tax/by-name-en/{nameEn}")
    public ResponseEntity<List<Tax>> findByNameEn(
            @NotNull
            Long facilityId,

            @PathVariable
            String nameEn,

            @ParameterObject
            Pageable pageable
    ) {
        return pagedResponse(
                taxService.findByNameEn(
                        facilityId,
                        nameEn,
                        pageable
                )
        );
    }

    @GetMapping("/tax/by-name-ar/{nameAr}")
    public ResponseEntity<List<Tax>> findByNameAr(
            @NotNull
            Long facilityId,

            @PathVariable
            String nameAr,

            @ParameterObject
            Pageable pageable
    ) {
        return pagedResponse(
                taxService.findByNameAr(
                        facilityId,
                        nameAr,
                        pageable
                )
        );
    }

    @GetMapping("/tax/by-type/{taxType}")
    public ResponseEntity<List<Tax>> findByTaxType(
            @NotNull
            Long facilityId,

            @PathVariable
            TaxType taxType,

            @ParameterObject
            Pageable pageable
    ) {
        return pagedResponse(
                taxService.findByTaxType(
                        facilityId,
                        taxType,
                        pageable
                )
        );
    }

    @GetMapping("/tax/by-calculation-type/{calculationType}")
    public ResponseEntity<List<Tax>> findByCalculationType(
            @NotNull
            Long facilityId,

            @PathVariable
            TaxCalculationType calculationType,

            @ParameterObject
            Pageable pageable
    ) {
        return pagedResponse(
                taxService.findByCalculationType(
                        facilityId,
                        calculationType,
                        pageable
                )
        );
    }

    @GetMapping("/tax/effective/{facilityId}")
    public ResponseEntity<List<Tax>> findEffectiveTaxes(
            @PathVariable
            @NotNull
            Long facilityId,

            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate date
    ) {
        return ResponseEntity.ok(
                taxService.findEffectiveTaxes(
                        facilityId,
                        date
                )
        );
    }

    @DeleteMapping("/tax/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable
            @NotNull
            Long id
    ) {
        boolean deleted = taxService.delete(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<List<Tax>> pagedResponse(
            Page<Tax> page
    ) {
        HttpHeaders headers =
                PaginationUtil.generatePaginationHttpHeaders(
                        ServletUriComponentsBuilder
                                .fromCurrentRequest(),
                        page
                );

        return new ResponseEntity<>(
                page.getContent(),
                headers,
                HttpStatus.OK
        );
    }
}
