package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Discount;
import com.dazzle.asklepios.domain.enumeration.DiscountApplicableOn;
import com.dazzle.asklepios.domain.enumeration.DiscountType;
import com.dazzle.asklepios.service.DiscountService;
import com.dazzle.asklepios.service.dto.DiscountDTO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class DiscountController {

    private static final Logger LOG =
            LoggerFactory.getLogger(DiscountController.class);

    private static final String ENTITY_NAME = "discount";

    private final DiscountService discountService;

    public DiscountController(
            DiscountService discountService
    ) {
        this.discountService = discountService;
    }

    @PostMapping("/discount")
    public ResponseEntity<Discount> create(
            @Valid
            @RequestBody
            @NotNull
            DiscountDTO discountDTO
    ) {
        LOG.debug(
                "REST request to create Discount payload={}",
                discountDTO
        );

        if (discountDTO.id() != null) {
            throw new BadRequestAlertException(
                    "A new discount cannot already have an id",
                    ENTITY_NAME,
                    "id.exists"
            );
        }

        Discount created =
                discountService.create(discountDTO);

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/setup/discount/"
                                        + created.getId()
                        )
                )
                .body(created);
    }

    @PutMapping("/discount/{id}")
    public ResponseEntity<Discount> update(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody @NotNull DiscountDTO discountDTO
    ) {
        LOG.debug("REST request to update Discount id={} payload={}",id, discountDTO);

        if (discountDTO.id() == null) {
            throw new BadRequestAlertException(
                    "Discount id is required",
                    ENTITY_NAME,
                    "id.required"
            );
        }

        if (!id.equals(discountDTO.id())) {
            throw new BadRequestAlertException(
                    "Path id and payload id do not match",
                    ENTITY_NAME,
                    "id.mismatch"
            );
        }

        return ResponseEntity.ok(
                discountService.update(
                        id,
                        discountDTO
                )
        );
    }

    @PatchMapping("/discount/{id}/toggle-active")
    public ResponseEntity<Discount> toggleActive(
            @PathVariable
            @NotNull
            Long id
    ) {
        return ResponseEntity.ok(
                discountService.toggleActive(id)
        );
    }

    @PutMapping(
            "/discount/{id}/activation-status/{active}"
    )
    public ResponseEntity<Discount> changeActivationStatus(
            @PathVariable
            @NotNull
            Long id,

            @PathVariable
            @NotNull
            Boolean active
    ) {
        return ResponseEntity.ok(
                discountService.changeActivationStatus(
                        id,
                        active
                )
        );
    }

    @PatchMapping("/discount/{id}/set-default")
    public ResponseEntity<Discount> setDefault(
            @PathVariable
            @NotNull
            Long id
    ) {
        return ResponseEntity.ok(
                discountService.setDefault(id)
        );
    }

    @GetMapping("/discount/{id}")
    public ResponseEntity<Discount> findById(
            @PathVariable
            @NotNull
            Long id
    ) {
        return ResponseEntity.ok(
                discountService.findById(id)
        );
    }

    @GetMapping("/discount")
    public ResponseEntity<List<Discount>> findAll(
            @RequestParam
            @NotNull
            Long facilityId,

            @ParameterObject
            Pageable pageable
    ) {
        Page<Discount> page =
                discountService.findAllByFacilityId(
                        facilityId,
                        pageable
                );

        return pagedResponse(page);
    }

    @GetMapping("/discount/active")
    public ResponseEntity<List<Discount>> findAllActive(
            @RequestParam
            @NotNull
            Long facilityId,

            @ParameterObject
            Pageable pageable
    ) {
        Page<Discount> page =
                discountService.findAllActiveByFacilityId(
                        facilityId,
                        pageable
                );

        return pagedResponse(page);
    }

    @GetMapping(
            "/discount/default/{facilityId}"
    )
    public ResponseEntity<Discount> findDefaultDiscount(
            @PathVariable
            @NotNull
            Long facilityId
    ) {
        return ResponseEntity.ok(
                discountService.findDefaultDiscount(
                        facilityId
                )
        );
    }

    @GetMapping("/discount/by-code/{code}")
    public ResponseEntity<List<Discount>> findByCode(
            @RequestParam
            @NotNull
            Long facilityId,

            @PathVariable
            String code,

            @ParameterObject
            Pageable pageable
    ) {
        return pagedResponse(
                discountService.findByCode(
                        facilityId,
                        code,
                        pageable
                )
        );
    }

    @GetMapping("/discount/by-name/{name}")
    public ResponseEntity<List<Discount>> findByName(
            @RequestParam
            @NotNull
            Long facilityId,

            @PathVariable
            String name,

            @ParameterObject
            Pageable pageable
    ) {
        return pagedResponse(
                discountService.findByName(
                        facilityId,
                        name,
                        pageable
                )
        );
    }

    @GetMapping(
            "/discount/by-type/{discountType}"
    )
    public ResponseEntity<List<Discount>> findByDiscountType(
            @RequestParam
            @NotNull
            Long facilityId,

            @PathVariable
            DiscountType discountType,

            @ParameterObject
            Pageable pageable
    ) {
        return pagedResponse(
                discountService.findByDiscountType(
                        facilityId,
                        discountType,
                        pageable
                )
        );
    }

    @GetMapping(
            "/discount/by-applicable-on/{applicableOn}"
    )
    public ResponseEntity<List<Discount>> findByApplicableOn(
            @RequestParam
            @NotNull
            Long facilityId,

            @PathVariable
            DiscountApplicableOn applicableOn,

            @ParameterObject
            Pageable pageable
    ) {
        return pagedResponse(
                discountService.findByApplicableOn(
                        facilityId,
                        applicableOn,
                        pageable
                )
        );
    }

    @GetMapping(
            "/discount/effective/{facilityId}"
    )
    public ResponseEntity<List<Discount>> findEffectiveDiscounts(
            @PathVariable
            @NotNull
            Long facilityId,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate date
    ) {
        return ResponseEntity.ok(
                discountService.findEffectiveDiscounts(
                        facilityId,
                        date
                )
        );
    }

    @GetMapping(
            "/discount/by-facility/{facilityId}/code/{code}"
    )
    public ResponseEntity<Discount>findByFacilityIdAndCode( @PathVariable @NotNull Long facilityId, @PathVariable  String code) {
        return ResponseEntity.ok( discountService.findByFacilityIdAndCode(facilityId,code
                )
        );
    }

    @DeleteMapping("/discount/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable
            @NotNull
            Long id
    ) {
        boolean deleted =
                discountService.delete(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<List<Discount>> pagedResponse(
            Page<Discount> page
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
