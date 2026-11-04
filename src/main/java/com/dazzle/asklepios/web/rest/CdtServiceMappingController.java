package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.CdtServiceMapping;
import com.dazzle.asklepios.service.CdtServiceMappingService;
import com.dazzle.asklepios.web.rest.vm.cdtservicemapping.CdtServiceMappingResponseVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for looking up {@link com.dazzle.asklepios.domain.CdtServiceMapping} records.
 */
@RestController
@RequestMapping("/api/setup/cdt-service-mappings")
public class CdtServiceMappingController {

    private static final Logger LOG = LoggerFactory.getLogger(CdtServiceMappingController.class);

    private final CdtServiceMappingService service;

    public CdtServiceMappingController(CdtServiceMappingService service) {
        this.service = service;
    }

    /**
     * {@code GET /api/setup/cdt-service-mappings/by-cdt/{cdtId}} :
     * Get all service mappings linked to a given CDT code.
     *
     * @param cdtId the ID of the CDT code.
     * @return {@link ResponseEntity} with the list of {@link CdtServiceMappingResponseVM}.
     */
    @GetMapping("/by-cdt/{cdtId}")
    public ResponseEntity<List<CdtServiceMappingResponseVM>> getMappingsForCdt(@PathVariable Long cdtId) {
        LOG.debug("REST request to get CdtServiceMappings by cdtId={}", cdtId);
        List<CdtServiceMapping> mappings = service.findByCdtCode(cdtId);

        return ResponseEntity.ok(
                mappings.stream()
                        .map(CdtServiceMappingResponseVM::fromEntity)
                        .toList()
        );
    }
}
