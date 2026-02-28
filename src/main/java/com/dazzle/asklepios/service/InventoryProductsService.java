package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.InventoryProducts;
import com.dazzle.asklepios.domain.enumeration.InventoryType;
import com.dazzle.asklepios.domain.enumeration.ProductTypes;
import com.dazzle.asklepios.repository.InventoryProductsRepository;
import com.dazzle.asklepios.service.dto.InventoryProductsCreateDTO;
import com.dazzle.asklepios.service.dto.InventoryProductsUpdateDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class InventoryProductsService {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryProductsService.class);

    private static final String ENTITY_NAME = "InventoryProducts";

    private final InventoryProductsRepository inventoryRepo;

    public InventoryProductsService(InventoryProductsRepository inventoryRepo) {
        this.inventoryRepo = inventoryRepo;
    }

    public InventoryProducts create(InventoryProductsCreateDTO inventoryProductsCreateDTO) {
        LOG.debug("create inventory product {}", inventoryProductsCreateDTO);

        validateUniqueConstraintsForCreate(inventoryProductsCreateDTO.name(), inventoryProductsCreateDTO.type(), inventoryProductsCreateDTO.code());
        InventoryProducts entity = toEntityForCreate(inventoryProductsCreateDTO);

        try {
            InventoryProducts saved = inventoryRepo.saveAndFlush(entity);
            LOG.debug("create: saved id={}", saved.getId());
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage());
            String lowerMessage = message != null ? message.toLowerCase() : "";

            LOG.error("Database constraint violation while creating InventoryProducts: {}", message, ex);

            if (lowerMessage.contains("idx_inventory_products_name_type_unique")) {

                throw new BadRequestAlertException(
                        "An inventory product with the same name already exists.",
                        ENTITY_NAME,
                        "unique.name.inventoryProducts"
                );
            } else if (lowerMessage.contains("idx_inventory_products_code_type_unique")) {

                throw new BadRequestAlertException(
                        "An inventory product with the same code already exists.",
                        ENTITY_NAME,
                        "unique.code.inventoryProducts"
                );
            }

            throw new BadRequestAlertException(
                    "Database constraint violated while creating inventory product (check unique name/barcode/code, or required fields).",
                    ENTITY_NAME,
                    "db.constraint"
            );
        }
    }

    public InventoryProducts update(InventoryProductsUpdateDTO inventoryProductsUpdateDTO) {
        LOG.debug("update inventory product: {}", inventoryProductsUpdateDTO);
        InventoryProducts entity = inventoryRepo.findById(inventoryProductsUpdateDTO.id())
                .orElseThrow(() -> new NotFoundAlertException("InventoryProducts not found: " + inventoryProductsUpdateDTO.id(), ENTITY_NAME, "notfound"));

        validateUniqueConstraintsForUpdate(inventoryProductsUpdateDTO, entity);
        applyUpdate(entity, inventoryProductsUpdateDTO);

        try {
            InventoryProducts saved = inventoryRepo.save(entity);
            LOG.debug("update: saved id={}", saved.getId());
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage());
            String lowerMessage = message != null ? message.toLowerCase() : "";

            LOG.error("Database constraint violation while updating InventoryProducts: {}", message, ex);

            if (lowerMessage.contains("idx_inventory_products_name_type_unique")) {
                throw new BadRequestAlertException("An inventory product with the same name already exists.", ENTITY_NAME, "unique.name.inventoryProducts");
            } else if (lowerMessage.contains("idx_inventory_products_code_type_unique")) {
                throw new BadRequestAlertException("An inventory product with the same code already exists.", ENTITY_NAME, "unique.name.inventoryProducts");
            }
            throw new BadRequestAlertException("Database constraint violated while updating inventory product (check unique name/barcode/code, or required fields).", ENTITY_NAME, "db.constraint"
            );
        }
    }

    @Transactional(readOnly = true)
    public Page<InventoryProducts> getAll(Pageable pageable) {
        LOG.debug("get all inventory products: page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return inventoryRepo.findAll(pageable);
    }
    @Transactional(readOnly = true)
    public Optional<InventoryProducts> findOne(Long id) {
        LOG.debug("Request to get inventoryProduct : {}", id);
        return inventoryRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<InventoryProducts> getByProductType(ProductTypes type, Pageable pageable) {
        LOG.debug("get inventory products by type: type={} page={} size={}",
                type, pageable.getPageNumber(), pageable.getPageSize());
        return inventoryRepo.findByType(type, pageable);
    }

    @Transactional(readOnly = true)
    public Page<InventoryProducts> getByName(String name, Pageable pageable) {
        LOG.debug("get inventory products by name: q='{}' page={} size={}",
                name, pageable.getPageNumber(), pageable.getPageSize());
        return inventoryRepo.findByNameContainsIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Page<InventoryProducts> getByBaseUom(String baseUom, Pageable pageable) {
        LOG.debug("get inventory products by baseUom: baseUom='{}' page={} size={}",
                baseUom, pageable.getPageNumber(), pageable.getPageSize());
        return inventoryRepo.findByBaseUom(baseUom, pageable);
    }

    @Transactional(readOnly = true)
    public Page<InventoryProducts> getByInventoryType(InventoryType inventoryType, Pageable pageable) {
        LOG.debug("get inventory products by inventoryType: inventoryType={} page={} size={}",
                inventoryType, pageable.getPageNumber(), pageable.getPageSize());
        return inventoryRepo.findByInventoryType(inventoryType, pageable);
    }

    public InventoryProducts toggleActive(Long id) {
        LOG.debug("toggleActive for inventory product: id={}", id);
        InventoryProducts entity = inventoryRepo.findById(id)
                .orElseThrow(() ->
                        new NotFoundAlertException("InventoryProducts not found: " + id, ENTITY_NAME, "notfound"));

        entity.setIsActive(Boolean.FALSE.equals(entity.getIsActive()));
        InventoryProducts saved = inventoryRepo.save(entity);
        LOG.debug("toggleActive for inventory product: id={} newIsActive={}", id, saved.getIsActive());
        return saved;
    }

    // Helpers

    private InventoryProducts toEntityForCreate(InventoryProductsCreateDTO inventoryProductsCreateDTO) {
        InventoryProducts ip = new InventoryProducts();
        ip.setName(inventoryProductsCreateDTO.name());
        ip.setType(inventoryProductsCreateDTO.type());
        ip.setCode(inventoryProductsCreateDTO.code());
        ip.setBarcode(inventoryProductsCreateDTO.barcode());
        ip.setBrandId(inventoryProductsCreateDTO.brandId());
        ip.setUomGroupId(inventoryProductsCreateDTO.uomGroupId());
        ip.setBaseUom(inventoryProductsCreateDTO.baseUom());
        ip.setDispenseUom(inventoryProductsCreateDTO.dispenseUom());
        ip.setControlledSubstance(inventoryProductsCreateDTO.controlledSubstance());
        ip.setHazardousBiohazardousTag(inventoryProductsCreateDTO.hazardousBiohazardousTag());
        ip.setAllergyRisk(inventoryProductsCreateDTO.allergyRisk());
        ip.setItemAverageCost(inventoryProductsCreateDTO.itemAverageCost());
        ip.setPricePerBaseUom(inventoryProductsCreateDTO.pricePerBaseUom());
        ip.setWarrantyStartDate(inventoryProductsCreateDTO.warrantyStartDate());
        ip.setWarrantyEndDate(inventoryProductsCreateDTO.warrantyEndDate());
        ip.setMaintenanceSchedule(inventoryProductsCreateDTO.maintenanceSchedule());
        ip.setMaintenanceScheduleType(inventoryProductsCreateDTO.maintenanceScheduleType());
        ip.setCriticalEquipment(inventoryProductsCreateDTO.criticalEquipment());
        ip.setCalibrationRequired(inventoryProductsCreateDTO.calibrationRequired());
        ip.setTrainingRequired(inventoryProductsCreateDTO.trainingRequired());
        ip.setBatchManaged(inventoryProductsCreateDTO.batchManaged());
        ip.setExpiryDateMandatory(inventoryProductsCreateDTO.expiryDateMandatory());
        ip.setReusable(inventoryProductsCreateDTO.reusable());
        ip.setInventoryType(inventoryProductsCreateDTO.inventoryType());
        ip.setShelfLife(inventoryProductsCreateDTO.shelfLife());
        ip.setLeadTime(inventoryProductsCreateDTO.leadTime());
        ip.setErpIntegrationId(inventoryProductsCreateDTO.erpIntegrationId());
        ip.setIsActive(inventoryProductsCreateDTO.isActive() != null ? inventoryProductsCreateDTO.isActive() : Boolean.TRUE);
        return ip;
    }

    private void applyUpdate(InventoryProducts ip, InventoryProductsUpdateDTO inventoryProductsUpdateDTO) {
        if (inventoryProductsUpdateDTO.name() != null) ip.setName(inventoryProductsUpdateDTO.name());
        if (inventoryProductsUpdateDTO.type() != null) ip.setType(inventoryProductsUpdateDTO.type());
        if (inventoryProductsUpdateDTO.code() != null) ip.setCode(inventoryProductsUpdateDTO.code());
        if (inventoryProductsUpdateDTO.barcode() != null) ip.setBarcode(inventoryProductsUpdateDTO.barcode());
        if (inventoryProductsUpdateDTO.brandId() != null) ip.setBrandId(inventoryProductsUpdateDTO.brandId());
        if (inventoryProductsUpdateDTO.uomGroupId() != null) ip.setUomGroupId(inventoryProductsUpdateDTO.uomGroupId());
        if (inventoryProductsUpdateDTO.baseUom() != null) ip.setBaseUom(inventoryProductsUpdateDTO.baseUom());
        if (inventoryProductsUpdateDTO.dispenseUom() != null) ip.setDispenseUom(inventoryProductsUpdateDTO.dispenseUom());
        if (inventoryProductsUpdateDTO.controlledSubstance() != null) ip.setControlledSubstance(inventoryProductsUpdateDTO.controlledSubstance());
        if (inventoryProductsUpdateDTO.hazardousBiohazardousTag() != null) ip.setHazardousBiohazardousTag(inventoryProductsUpdateDTO.hazardousBiohazardousTag());
        if (inventoryProductsUpdateDTO.allergyRisk() != null) ip.setAllergyRisk(inventoryProductsUpdateDTO.allergyRisk());
        if (inventoryProductsUpdateDTO.itemAverageCost() != null) ip.setItemAverageCost(inventoryProductsUpdateDTO.itemAverageCost());
        if (inventoryProductsUpdateDTO.pricePerBaseUom() != null) ip.setPricePerBaseUom(inventoryProductsUpdateDTO.pricePerBaseUom());
        if (inventoryProductsUpdateDTO.warrantyStartDate() != null) ip.setWarrantyStartDate(inventoryProductsUpdateDTO.warrantyStartDate());
        if (inventoryProductsUpdateDTO.warrantyEndDate() != null) ip.setWarrantyEndDate(inventoryProductsUpdateDTO.warrantyEndDate());
        if (inventoryProductsUpdateDTO.maintenanceSchedule() != null) ip.setMaintenanceSchedule(inventoryProductsUpdateDTO.maintenanceSchedule());
        if (inventoryProductsUpdateDTO.maintenanceScheduleType() != null) ip.setMaintenanceScheduleType(inventoryProductsUpdateDTO.maintenanceScheduleType());
        if (inventoryProductsUpdateDTO.criticalEquipment() != null) ip.setCriticalEquipment(inventoryProductsUpdateDTO.criticalEquipment());
        if (inventoryProductsUpdateDTO.calibrationRequired() != null) ip.setCalibrationRequired(inventoryProductsUpdateDTO.calibrationRequired());
        if (inventoryProductsUpdateDTO.trainingRequired() != null) ip.setTrainingRequired(inventoryProductsUpdateDTO.trainingRequired());
        if (inventoryProductsUpdateDTO.batchManaged() != null) ip.setBatchManaged(inventoryProductsUpdateDTO.batchManaged());
        if (inventoryProductsUpdateDTO.expiryDateMandatory() != null) ip.setExpiryDateMandatory(inventoryProductsUpdateDTO.expiryDateMandatory());
        if (inventoryProductsUpdateDTO.reusable() != null) ip.setReusable(inventoryProductsUpdateDTO.reusable());
        if (inventoryProductsUpdateDTO.inventoryType() != null) ip.setInventoryType(inventoryProductsUpdateDTO.inventoryType());
        if (inventoryProductsUpdateDTO.shelfLife() != null) ip.setShelfLife(inventoryProductsUpdateDTO.shelfLife());
        if (inventoryProductsUpdateDTO.leadTime() != null) ip.setLeadTime(inventoryProductsUpdateDTO.leadTime());
        if (inventoryProductsUpdateDTO.erpIntegrationId() != null) ip.setErpIntegrationId(inventoryProductsUpdateDTO.erpIntegrationId());
        if (inventoryProductsUpdateDTO.isActive() != null) ip.setIsActive(inventoryProductsUpdateDTO.isActive());
    }

    private void validateUniqueConstraintsForCreate(String name, ProductTypes type, String code) {
        if (inventoryRepo.existsByNameIgnoreCaseAndType(name, type)) {
            throw new BadRequestAlertException("Inventory product name already exists for this type.", ENTITY_NAME, "unique.name.type");
        }

        if (code != null && inventoryRepo.existsByCodeIgnoreCaseAndType(code, type)) {
            throw new BadRequestAlertException("Inventory product code already exists for this type.", ENTITY_NAME, "unique.code.type"
            );
        }
    }

    private void validateUniqueConstraintsForUpdate(InventoryProductsUpdateDTO inventoryProductsUpdateDTO, InventoryProducts existing) {
        if (inventoryProductsUpdateDTO.name() != null && !inventoryProductsUpdateDTO.name().equalsIgnoreCase(existing.getName())) {
            if (inventoryRepo.existsByNameIgnoreCaseAndType(inventoryProductsUpdateDTO.name(), existing.getType())) {
                throw new BadRequestAlertException("Inventory product name already exists for this type.", ENTITY_NAME, "unique.name.type");
            }
        }
        if (inventoryProductsUpdateDTO.code() != null && !Objects.equals(inventoryProductsUpdateDTO.code(), existing.getCode())) {
            if (inventoryRepo.existsByCodeIgnoreCaseAndType(inventoryProductsUpdateDTO.code(), existing.getType())) {
                throw new BadRequestAlertException("Inventory product code already exists for this type.", ENTITY_NAME, "unique.code.type");
            }
        }
    }
}