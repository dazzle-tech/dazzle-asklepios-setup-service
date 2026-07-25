package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.FinancialDocumentNumbering;
import com.dazzle.asklepios.domain.enumeration.BillingConfigurationStatus;
import com.dazzle.asklepios.domain.enumeration.biling.FinancialDocumentType;
import com.dazzle.asklepios.service.FinancialDocumentNumberingService;
import com.dazzle.asklepios.service.dto.FinancialDocumentNumberingBulkDTO;
import com.dazzle.asklepios.service.dto.FinancialDocumentNumberingDTO;
import com.dazzle.asklepios.service.dto.FinancialDocumentSequenceStatusDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class FinancialDocumentNumberingController {

    private static final Logger LOG =
            LoggerFactory.getLogger(
                    FinancialDocumentNumberingController.class
            );

    private static final String ENTITY_NAME =
            "financialDocumentNumbering";

    private final FinancialDocumentNumberingService
            financialDocumentNumberingService;

    public FinancialDocumentNumberingController(
            FinancialDocumentNumberingService financialDocumentNumberingService
    ) {
        this.financialDocumentNumberingService =
                financialDocumentNumberingService;
    }

    @PostMapping("/financial-document-numbering")
    public ResponseEntity<FinancialDocumentNumberingDTO>
    create(
            @Valid
            @RequestBody
            @NotNull
            FinancialDocumentNumberingDTO dto
    ) {
        LOG.debug(
                "REST request to create FinancialDocumentNumbering payload={}",
                dto
        );

        if (
                dto.id() != null
        ) {
            throw new BadRequestAlertException(
                    "A new financial document numbering cannot already have an id",
                    ENTITY_NAME,
                    "id.exists"
            );
        }

        FinancialDocumentNumbering created =
                financialDocumentNumberingService.create(
                        dto
                );

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/setup/financial-document-numbering/"
                                        + created.getId()
                        )
                )
                .body(
                        financialDocumentNumberingService.toDto(
                                created
                        )
                );
    }

    @PutMapping("/financial-document-numbering/{id}")
    public ResponseEntity<FinancialDocumentNumberingDTO>
    update(
            @PathVariable("id")
            @NotNull
            Long id,

            @Valid
            @RequestBody
            @NotNull
            FinancialDocumentNumberingDTO dto
    ) {
        LOG.debug(
                "REST request to update FinancialDocumentNumbering id={} payload={}",
                id,
                dto
        );

        FinancialDocumentNumbering updated =
                financialDocumentNumberingService.update(
                        id,
                        dto
                );

        return ResponseEntity.ok(
                financialDocumentNumberingService.toDto(
                        updated
                )
        );
    }

    @PutMapping(
            "/financial-document-numbering/by-facility/{facilityId}"
    )
    public ResponseEntity<List<FinancialDocumentNumberingDTO>>
    saveBulk(
            @PathVariable("facilityId")
            @NotNull
            Long facilityId,

            @Valid
            @RequestBody
            @NotNull
            FinancialDocumentNumberingBulkDTO bulkDto
    ) {
        LOG.debug(
                "REST request to bulk save FinancialDocumentNumbering facilityId={}",
                facilityId
        );

        if (
                !facilityId.equals(
                        bulkDto.facilityId()
                )
        ) {
            throw new BadRequestAlertException(
                    "Path facility id and payload facility id do not match",
                    ENTITY_NAME,
                    "facility.mismatch"
            );
        }

        List<FinancialDocumentNumbering> saved =
                financialDocumentNumberingService.saveBulk(
                        bulkDto
                );

        return ResponseEntity.ok(
                saved
                        .stream()
                        .map(
                                financialDocumentNumberingService::toDto
                        )
                        .toList()
        );
    }

    @GetMapping("/financial-document-numbering/{id}")
    public ResponseEntity<FinancialDocumentNumberingDTO>
    findById(
            @PathVariable("id")
            @NotNull
            Long id
    ) {
        FinancialDocumentNumbering entity =
                financialDocumentNumberingService.findById(
                        id
                );

        return ResponseEntity.ok(
                financialDocumentNumberingService.toDto(
                        entity
                )
        );
    }

    @GetMapping(
            "/financial-document-numbering/by-facility/{facilityId}"
    )
    public ResponseEntity<List<FinancialDocumentNumberingDTO>>
    findByFacilityId(
            @PathVariable("facilityId")
            @NotNull
            Long facilityId
    ) {
        List<FinancialDocumentNumbering> entities =
                financialDocumentNumberingService.findByFacilityId(
                        facilityId
                );

        return ResponseEntity.ok(
                entities
                        .stream()
                        .map(
                                financialDocumentNumberingService::toDto
                        )
                        .toList()
        );
    }

    @GetMapping(
            "/financial-document-numbering/by-facility/{facilityId}/document-type/{documentType}"
    )
    public ResponseEntity<FinancialDocumentNumberingDTO>
    findByFacilityIdAndDocumentType(
            @PathVariable("facilityId")
            @NotNull
            Long facilityId,

            @PathVariable("documentType")
            @NotNull
            FinancialDocumentType documentType
    ) {
        FinancialDocumentNumbering entity =
                financialDocumentNumberingService
                        .findByFacilityIdAndDocumentType(
                                facilityId,
                                documentType
                        );

        return ResponseEntity.ok(
                financialDocumentNumberingService.toDto(
                        entity
                )
        );
    }

    @GetMapping(
            "/financial-document-numbering/by-facility/{facilityId}/sequence-status/{documentType}"
    )
    public ResponseEntity<List<FinancialDocumentSequenceStatusDTO>>
    getSequenceStatus(
            @PathVariable("facilityId")
            @NotNull
            Long facilityId,

            @PathVariable("documentType")
            @NotNull
            FinancialDocumentType documentType
    ) {
        return ResponseEntity.ok(
                financialDocumentNumberingService.getSequenceStatus(
                        facilityId,
                        documentType
                )
        );
    }

    @PutMapping(
            "/financial-document-numbering/{id}/activation-status/{active}"
    )
    public ResponseEntity<FinancialDocumentNumberingDTO>
    changeActivationStatus(
            @PathVariable("id")
            @NotNull
            Long id,

            @PathVariable("active")
            @NotNull
            Boolean active
    ) {
        FinancialDocumentNumbering updated =
                financialDocumentNumberingService.changeActivationStatus(
                        id,
                        active
                );

        return ResponseEntity.ok(
                financialDocumentNumberingService.toDto(
                        updated
                )
        );
    }

    @PutMapping(
            "/financial-document-numbering/{id}/status/{status}"
    )
    public ResponseEntity<FinancialDocumentNumberingDTO>
    changeStatus(
            @PathVariable("id")
            @NotNull
            Long id,

            @PathVariable("status")
            @NotNull
            BillingConfigurationStatus status
    ) {
        FinancialDocumentNumbering updated =
                financialDocumentNumberingService.changeStatus(
                        id,
                        status
                );

        return ResponseEntity.ok(
                financialDocumentNumberingService.toDto(
                        updated
                )
        );
    }

    @DeleteMapping("/financial-document-numbering/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id")
            @NotNull
            Long id
    ) {
        boolean deleted =
                financialDocumentNumberingService.delete(
                        id
                );

        if (
                !deleted
        ) {
            throw new BadRequestAlertException(
                    "Unable to delete financial document numbering",
                    ENTITY_NAME,
                    "delete.failed"
            );
        }

        return ResponseEntity.noContent().build();
    }
}
