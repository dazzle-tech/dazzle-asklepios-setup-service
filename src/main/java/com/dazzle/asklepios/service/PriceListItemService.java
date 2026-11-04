package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.PriceListItem;
import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.repository.BrandMedicationRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.PriceListItemRepository;
import com.dazzle.asklepios.repository.ProcedureRepository;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemSaveVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class PriceListItemService {

    private static final Logger LOG = LoggerFactory.getLogger(PriceListItemService.class);

    private final PriceListItemRepository repo;
    private final ServiceRepository serviceRepository;
    private final BrandMedicationRepository brandMedicationRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final ProcedureRepository procedureRepository;

    public PriceListItemService(
            PriceListItemRepository repo,
            ServiceRepository serviceRepository,
            BrandMedicationRepository brandMedicationRepository,
            DiagnosticTestRepository diagnosticTestRepository,
            ProcedureRepository procedureRepository
    ) {
        this.repo = repo;
        this.serviceRepository = serviceRepository;
        this.brandMedicationRepository = brandMedicationRepository;
        this.diagnosticTestRepository = diagnosticTestRepository;
        this.procedureRepository = procedureRepository;
    }

    public PriceListItem create(PriceListItemSaveVM vm) {
        LOG.info("[CREATE] Request to create PriceListItem payload={}", vm);


        validateNoDuplicate(
                vm.priceListId(),
                vm.itemType(),
                vm.serviceId(),
                vm.brandMedicationId(),
                vm.diagnosticTestId(),
                vm.procedureId(),
                null
        );

        PriceListItem entity = PriceListItem.builder()
                .priceListId(vm.priceListId())
                .itemType(vm.itemType())
                .price(vm.price())
                .discountAllowed(vm.discountAllowed() != null ? vm.discountAllowed() : false)
                .isActive(vm.isActive() != null ? vm.isActive() : true)
                .build();

        assignTarget(entity, vm.itemType(), vm.serviceId(), vm.brandMedicationId(), vm.diagnosticTestId(), vm.procedureId());

        try {
            PriceListItem saved = repo.saveAndFlush(entity);
            LOG.info("[CREATE] Created PriceListItem id={}", saved.getId());
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            throw handleConstraintViolation(ex, "saving");
        }
    }

    public PriceListItem update(PriceListItemUpdateVM vm) {
        LOG.info("[UPDATE] Request to update PriceListItem id={} payload={}", vm.id(), vm);

        PriceListItem entity = repo.findById(vm.id())
                .orElseThrow(() -> new NotFoundAlertException(
                        "Price list item not found with id " + vm.id(),
                        "priceListItem",
                        "notfound"
                ));

        validateNoDuplicate(
                vm.priceListId(),
                vm.itemType(),
                vm.serviceId(),
                vm.brandMedicationId(),
                vm.diagnosticTestId(),
                vm.procedureId(),
                vm.id()
        );

        entity.setPriceListId(vm.priceListId());
        entity.setItemType(vm.itemType());
        entity.setPrice(vm.price());
        entity.setDiscountAllowed(vm.discountAllowed() != null ? vm.discountAllowed() : entity.getDiscountAllowed());
        entity.setIsActive(vm.isActive() != null ? vm.isActive() : entity.getIsActive());

        clearTargets(entity);
        assignTarget(entity, vm.itemType(), vm.serviceId(), vm.brandMedicationId(), vm.diagnosticTestId(), vm.procedureId());

        try {
            PriceListItem updated = repo.saveAndFlush(entity);
            LOG.info("[UPDATE] Updated PriceListItem id={}", updated.getId());
            return updated;
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            throw handleConstraintViolation(ex, "updating");
        }
    }

    private void validateNoDuplicate(
            Long priceListId,
            BillingItemTypes type,
            Long serviceId,
            Long brandMedicationId,
            Long diagnosticTestId,
            Long procedureId,
            Long currentId
    ) {
        Long excludedId = currentId == null ? -1L : currentId;

        boolean exists = switch (type) {
            case SERVICE -> repo.existsByPriceListIdAndService_IdAndIdNot(priceListId, serviceId, excludedId);
            case MEDICATION -> repo.existsByPriceListIdAndBrandMedication_IdAndIdNot(priceListId, brandMedicationId, excludedId);
            case LABORATORY, RADIOLOGY, PATHOLOGY ->
                    repo.existsByPriceListIdAndDiagnosticTest_IdAndIdNot(priceListId, diagnosticTestId, excludedId);
            case PROCEDURE -> repo.existsByPriceListIdAndProcedure_IdAndIdNot(priceListId, procedureId, excludedId);
            default -> false;
        };

        if (exists) {
            throw new BadRequestAlertException(
                    "duplicateItem",
                    "priceListItem",
                    "This item already exists in the same price list"
            );
        }
    }

    private void assignTarget(
            PriceListItem entity,
            BillingItemTypes itemType,
            Long serviceId,
            Long brandMedicationId,
            Long diagnosticTestId,
            Long procedureId
    ) {
        switch (itemType) {
            case SERVICE -> entity.setService(findService(serviceId));
            case MEDICATION -> entity.setBrandMedication(findBrandMedication(brandMedicationId));
            case LABORATORY, RADIOLOGY, PATHOLOGY -> entity.setDiagnosticTest(findDiagnosticTest(diagnosticTestId));
            case PROCEDURE -> entity.setProcedure(findProcedure(procedureId));
            default -> throw new BadRequestAlertException(
                    "unsupportedItemType",
                    "priceListItem",
                    "Unsupported item type: " + itemType
            );
        }
    }

    private void clearTargets(PriceListItem entity) {
        entity.setService(null);
        entity.setBrandMedication(null);
        entity.setDiagnosticTest(null);
        entity.setProcedure(null);
    }

    private ServiceSetup findService(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Service not found with id " + id,
                        "service",
                        "notfound"
                ));
    }

    private BrandMedication findBrandMedication(Long id) {
        return brandMedicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Brand medication not found with id " + id,
                        "brandMedication",
                        "notfound"
                ));
    }

    private DiagnosticTest findDiagnosticTest(Long id) {
        return diagnosticTestRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Diagnostic test not found with id " + id,
                        "diagnosticTest",
                        "notfound"
                ));
    }

    private Procedure findProcedure(Long id) {
        return procedureRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Procedure not found with id " + id,
                        "procedure",
                        "notfound"
                ));
    }

    private BadRequestAlertException handleConstraintViolation(RuntimeException ex, String action) {
        Throwable root = getRootCause(ex);
        String message = root != null ? root.getMessage() : ex.getMessage();
        String lower = message != null ? message.toLowerCase() : "";

        LOG.error("[DB CONSTRAINT] Error while {} PriceListItem. Root cause={}", action, message, ex);

        if (lower.contains("fk_pl_item_service")) {
            return new BadRequestAlertException("serviceNotFound", "priceListItem", "Service does not exist");
        }

        if (lower.contains("fk_pl_item_brand_medication")) {
            return new BadRequestAlertException("brandMedicationNotFound", "priceListItem", "Brand medication does not exist");
        }

        if (lower.contains("fk_pl_item_diagnostic_test")) {
            return new BadRequestAlertException("diagnosticTestNotFound", "priceListItem", "Diagnostic test does not exist");
        }

        if (lower.contains("fk_pl_item_procedure")) {
            return new BadRequestAlertException("procedureNotFound", "priceListItem", "Procedure does not exist");
        }

        if (lower.contains("ck_pl_item_reference_by_type")) {
            return new BadRequestAlertException(
                    "invalidItemReference",
                    "priceListItem",
                    "Invalid item reference for selected item type"
            );
        }

        if (
                lower.contains("uq_pl_item_service") ||
                        lower.contains("uq_pl_item_brand_medication") ||
                        lower.contains("uq_pl_item_diagnostic_test") ||
                        lower.contains("uq_pl_item_procedure")
        ) {
            return new BadRequestAlertException(
                    "duplicateItem",
                    "priceListItem",
                    "This item already exists in the same price list"
            );
        }

        if (lower.contains("not-null") || lower.contains("null value")) {
            return new BadRequestAlertException(
                    "requiredFields",
                    "priceListItem",
                    "Required fields are missing"
            );
        }

        return new BadRequestAlertException(
                "db.constraint",
                "priceListItem",
                "Database constraint violated while " + action + " price list item"
        );
    }

    @Transactional(readOnly = true)
    public Page<PriceListItem> findAll(Pageable pageable) {
        LOG.debug("[FIND ALL] Fetch PriceListItems page={}", pageable);
        return repo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<PriceListItem> findAllActive(Pageable pageable) {
        LOG.debug("[FIND ACTIVE] Fetch active PriceListItems page={}", pageable);
        return repo.findByIsActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<PriceListItem> findByPriceList(Long priceListId, Pageable pageable) {
        LOG.debug("[FIND BY PRICE LIST] priceListId={} page={}", priceListId, pageable);
        return repo.findByPriceListId(priceListId, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<PriceListItem> findOne(Long id) {
        LOG.debug("[FIND ONE] PriceListItem id={}", id);
        return repo.findById(id);
    }

    public Optional<PriceListItem> toggleIsActive(Long id) {
        LOG.info("[TOGGLE ACTIVE] PriceListItem id={}", id);

        return repo.findById(id)
                .map(item -> {
                    item.setIsActive(!Boolean.TRUE.equals(item.getIsActive()));
                    PriceListItem saved = repo.save(item);
                    LOG.info("[TOGGLE ACTIVE] PriceListItem id={} active={}", saved.getId(), saved.getIsActive());
                    return saved;
                });
    }
}