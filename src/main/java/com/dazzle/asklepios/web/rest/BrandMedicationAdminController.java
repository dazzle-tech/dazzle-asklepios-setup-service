package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.BrandMedicationImportConversionService;
import com.dazzle.asklepios.service.dto.BrandMedicationCsvConversionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/admin/brand-medications")
public class BrandMedicationAdminController {

    private static final Logger LOG = LoggerFactory.getLogger(BrandMedicationAdminController.class);

    private final BrandMedicationImportConversionService conversionService;

    public BrandMedicationAdminController(BrandMedicationImportConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Resource> convertBrandMedicationExcelToCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("billingRuleId") Long billingRuleId,
            @RequestParam("dosageForm") String dosageForm
    ) {
        LOG.debug(
                "REST request to convert BrandMedication import file. fileName='{}', billingRuleId={}, dosageForm='{}'",
                file != null ? file.getOriginalFilename() : null,
                billingRuleId,
                dosageForm
        );

        BrandMedicationCsvConversionResult result = conversionService.convert(file, billingRuleId, dosageForm);

        String outputFileName = buildOutputFileName(file != null ? file.getOriginalFilename() : null);
        ByteArrayResource resource = new ByteArrayResource(result.csvContent());

        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(outputFileName).build().toString())
                .header("X-Total-Records", String.valueOf(result.totalRecords()))
                .header("X-Successful-Records", String.valueOf(result.successfulRecords()))
                .header("X-Skipped-Records", String.valueOf(result.skippedRecords()))
                .contentLength(result.csvContent().length)
                .body(resource);
    }

    private String buildOutputFileName(String inputFileName) {
        if (inputFileName == null || inputFileName.isBlank()) {
            return "brand-medications-import.csv";
        }

        int dotIndex = inputFileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? inputFileName.substring(0, dotIndex) : inputFileName;
        return baseName.trim().replaceAll("\\s+", "-") + "-converted.csv";
    }
}

