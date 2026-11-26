package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.PayorSupportiveDocumentRequirementService;
import com.dazzle.asklepios.web.rest.vm.supportivedocument.PayorSupportiveDocumentRequirementResponseVM;
import com.dazzle.asklepios.web.rest.vm.supportivedocument.PayorSupportiveDocumentRequirementUpdateVM;
import com.dazzle.asklepios.web.rest.vm.supportivedocument.SupportiveDocumentMedicalSheetAvailabilityVM;
import com.dazzle.asklepios.web.rest.vm.supportivedocument.SupportiveDocumentRequirementCatalogVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class SupportiveDocumentRequirementController {

    private static final Logger LOG =
            LoggerFactory.getLogger(SupportiveDocumentRequirementController.class);

    private final PayorSupportiveDocumentRequirementService service;

    public SupportiveDocumentRequirementController(PayorSupportiveDocumentRequirementService service) {
        this.service = service;
    }

    @GetMapping("/supportive-document-requirements")
    public ResponseEntity<List<SupportiveDocumentRequirementCatalogVM>> getCatalog() {
        LOG.debug("REST get supportive-document requirement catalog");
        return ResponseEntity.ok(service.getCatalog());
    }

    /**
     * Exhaustive MedicalSheets availability for Setup display (supported + disabled unsupported).
     * Does not replace the requirement catalog; codes remain on getCatalog().
     */
    @GetMapping("/supportive-document-medical-sheets")
    public ResponseEntity<List<SupportiveDocumentMedicalSheetAvailabilityVM>> getMedicalSheets() {
        LOG.debug("REST get supportive-document medical sheet availability");
        return ResponseEntity.ok(service.getMedicalSheetAvailability());
    }

    @GetMapping("/payor/{payorId}/supportive-document-requirements")
    public ResponseEntity<PayorSupportiveDocumentRequirementResponseVM> getByPayor(
            @PathVariable Long payorId
    ) {
        LOG.debug("REST get supportive-document requirements payorId={}", payorId);
        return ResponseEntity.ok(service.getByPayorId(payorId));
    }

    @PutMapping("/payor/{payorId}/supportive-document-requirements")
    public ResponseEntity<PayorSupportiveDocumentRequirementResponseVM> replace(
            @PathVariable Long payorId,
            @RequestBody PayorSupportiveDocumentRequirementUpdateVM vm
    ) {
        LOG.debug("REST replace supportive-document requirements payorId={} payload={}", payorId, vm);
        return ResponseEntity.ok(service.replace(payorId, vm));
    }
}
