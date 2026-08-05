package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.FinancialDocumentNumberingService;
import com.dazzle.asklepios.service.dto.FinancialDocumentNumberRequest;
import com.dazzle.asklepios.service.dto.FinancialDocumentNumberResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class FinancialDocumentNumberInternalController {

    private static final Logger LOG =
            LoggerFactory.getLogger(
                    FinancialDocumentNumberInternalController.class
            );

    private final FinancialDocumentNumberingService
            financialDocumentNumberingService;

    @PostMapping("/internal/financial-document-number/next")
    public ResponseEntity<FinancialDocumentNumberResponse>
    generateNextNumber(
            @Valid
            @RequestBody
            FinancialDocumentNumberRequest request
    ) {
        LOG.debug(
                "REST request to generate next financial document number request={}",
                request
        );

        FinancialDocumentNumberResponse response =
                financialDocumentNumberingService
                        .generateNextNumber(
                                request
                        );

        return ResponseEntity.ok(
                response
        );
    }
}
