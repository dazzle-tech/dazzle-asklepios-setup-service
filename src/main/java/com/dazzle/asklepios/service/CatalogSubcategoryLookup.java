package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.ProcedureCategory;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.repository.DiagnosticTestLaboratoryRepository;
import com.dazzle.asklepios.repository.DiagnosticTestPathologyRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRadiologyRepository;
import com.dazzle.asklepios.repository.ProcedureRepository;
import com.dazzle.asklepios.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogSubcategoryLookup {

    private final DiagnosticTestLaboratoryRepository laboratoryRepository;
    private final DiagnosticTestRadiologyRepository radiologyRepository;
    private final DiagnosticTestPathologyRepository pathologyRepository;
    private final ProcedureRepository procedureRepository;
    private final ServiceRepository serviceRepository;

    public CatalogSubcategoryLookup(
            DiagnosticTestLaboratoryRepository laboratoryRepository,
            DiagnosticTestRadiologyRepository radiologyRepository,
            DiagnosticTestPathologyRepository pathologyRepository,
            ProcedureRepository procedureRepository,
            ServiceRepository serviceRepository
    ) {
        this.laboratoryRepository = laboratoryRepository;
        this.radiologyRepository = radiologyRepository;
        this.pathologyRepository = pathologyRepository;
        this.procedureRepository = procedureRepository;
        this.serviceRepository = serviceRepository;
    }

    public String subcategoryOf(BillingItemTypes type, Long catalogItemId) {
        if (type == null || catalogItemId == null) {
            return null;
        }
        String value = switch (type) {
            case LABORATORY -> laboratoryRepository.findByTestId(catalogItemId)
                    .map(row -> row.getCategory())
                    .orElse(null);
            case RADIOLOGY -> radiologyRepository.findByTestId(catalogItemId)
                    .map(row -> row.getCategory())
                    .orElse(null);
            case PATHOLOGY -> pathologyRepository.findByTest_Id(catalogItemId)
                    .map(row -> row.getCategory())
                    .orElse(null);
            case PROCEDURE -> procedureRepository.findById(catalogItemId)
                    .map(row -> row.getCategoryType() == null ? null : row.getCategoryType().name())
                    .orElse(null);
            case SERVICE -> serviceRepository.findById(catalogItemId)
                    .map(row -> row.getCategory() == null ? null : row.getCategory().name())
                    .orElse(null);
            default -> null;
        };
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * Catalog item ids that belong to a subcategory, used to exclude a whole
     * subcategory from a Coverage Limit.
     */
    public List<Long> catalogItemIdsOf(BillingItemTypes type, String subcategoryKey) {
        if (type == null || subcategoryKey == null || subcategoryKey.isBlank()) {
            return List.of();
        }
        String key = subcategoryKey.trim();
        try {
            return switch (type) {
                case LABORATORY -> laboratoryRepository.findByCategoryIgnoreCase(key).stream()
                        .map(row -> row.getTest().getId())
                        .toList();
                case RADIOLOGY -> radiologyRepository.findByCategoryIgnoreCase(key).stream()
                        .map(row -> row.getTest().getId())
                        .toList();
                case PATHOLOGY -> pathologyRepository.findByCategoryIgnoreCase(key).stream()
                        .map(row -> row.getTest().getId())
                        .toList();
                case PROCEDURE -> procedureRepository.findByCategoryType(ProcedureCategory.valueOf(key)).stream()
                        .map(Procedure::getId)
                        .toList();
                case SERVICE -> serviceRepository.findByCategory(ServiceCategory.valueOf(key)).stream()
                        .map(ServiceSetup::getId)
                        .toList();
                default -> List.of();
            };
        } catch (IllegalArgumentException ex) {
            return List.of();
        }
    }
}
