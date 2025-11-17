package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.repository.BrandMedicationRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.brandMedication.BrandMedicationCreateVM;
import com.dazzle.asklepios.web.rest.vm.brandMedication.BrandMedicationUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class BrandMedicationService {

    private static final Logger LOG = LoggerFactory.getLogger(BrandMedicationService.class);
    private final BrandMedicationRepository brandMedicationRepository;

    public BrandMedicationService(BrandMedicationRepository brandMedicationRepository) {
        this.brandMedicationRepository = brandMedicationRepository;
    }

    public BrandMedication create(BrandMedicationCreateVM vm) {
        LOG.debug("Request to create BrandMedication : {}", vm);

        if (vm.name() == null || vm.name().isBlank()) {
            throw new BadRequestAlertException("name is required", "brandMedication", "namerequired");
        }
        if (vm.dosageForm() == null || vm.dosageForm().isBlank()) {
            throw new BadRequestAlertException("dosageForm is required", "brandMedication", "dosageformrequired");
        }

//        if (vm.uomGroup() == null || vm.uomGroup().isBlank()) {
//            throw new BadRequestAlertException("uomGroup is required", "brandMedication", "uomGrouprequired");
//        }
//        if (vm.uomGroupUnit() == null || vm.uomGroupUnit().isBlank()) {
//            throw new BadRequestAlertException("uomGroupUnit is required", "brandMedication", "uomGroupUnitrequired");
//        }

        BrandMedication entity = BrandMedication.builder()
                .name(vm.name())
                .manufacturer(vm.manufacturer())
                .dosageForm(vm.dosageForm())
                .usageInstructions(vm.usageInstructions())
                .storageRequirements(vm.storageRequirements())
                .expiresAfterOpening(vm.expiresAfterOpening())
                .expiresAfterOpeningValue(vm.expiresAfterOpeningValue())
                .expiresAfterOpeningUnit(vm.expiresAfterOpeningUnit())
                .useSinglePatient(vm.useSinglePatient())
                .highCostMedication(vm.highCostMedication())
                .costCategory(vm.costCategory())
                .roa(vm.roa())
                .isActive(vm.isActive())
                .build();

        BrandMedication saved = brandMedicationRepository.save(entity);
        LOG.debug("Created BrandMedication: {}", saved);
        return saved;
    }

    public Optional<BrandMedication> update(Long id, BrandMedicationUpdateVM vm) {
        LOG.debug("Request to update BrandMedication id={} with {}", id, vm);

        BrandMedication entity = brandMedicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "BrandMedication not found with id " + id,
                        "brandMedication",
                        "notfound"
                ));

        if (vm.name() != null) entity.setName(vm.name());
        if (vm.manufacturer() != null) entity.setManufacturer(vm.manufacturer());
        if (vm.dosageForm() != null) entity.setDosageForm(vm.dosageForm());
        if (vm.usageInstructions() != null) entity.setUsageInstructions(vm.usageInstructions());
        if (vm.storageRequirements() != null) entity.setStorageRequirements(vm.storageRequirements());
        if (vm.expiresAfterOpening() != null) entity.setExpiresAfterOpening(vm.expiresAfterOpening());
        if (vm.expiresAfterOpeningValue() != null) entity.setExpiresAfterOpeningValue(vm.expiresAfterOpeningValue());
        if (vm.expiresAfterOpeningUnit() != null) entity.setExpiresAfterOpeningUnit(vm.expiresAfterOpeningUnit());
        if (vm.useSinglePatient() != null) entity.setUseSinglePatient(vm.useSinglePatient());
        if (vm.highCostMedication() != null) entity.setHighCostMedication(vm.highCostMedication());
        if (vm.costCategory() != null) entity.setCostCategory(vm.costCategory());
        if (vm.roa() != null) entity.setRoa(vm.roa());
        if (vm.isActive() != null) entity.setIsActive(vm.isActive());

        BrandMedication updated = brandMedicationRepository.save(entity);
        LOG.debug("Updated BrandMedication: {}", updated);
        return Optional.of(updated);
    }

    @Transactional(readOnly = true)
    public Page<BrandMedication> findAll(Pageable pageable) {
        LOG.debug("Request to get BrandMedications: {}", pageable);
        return brandMedicationRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<BrandMedication> findOne(Long id) {
        LOG.debug("Request to get BrandMedication : {}", id);
        return brandMedicationRepository.findById(id);
    }

    public Optional<BrandMedication> toggleIsActive(Long id) {
        LOG.debug("Request to toggle BrandMedication isActive id={}", id);
        return brandMedicationRepository.findById(id)
                .map(entity -> {
                    boolean isActive = !Boolean.TRUE.equals(entity.getIsActive());
                    entity.setIsActive(isActive);
                    return brandMedicationRepository.save(entity);
                });
    }

    @Transactional(readOnly = true)
    public Page<BrandMedication> findByName(String name, Pageable pageable) {
        LOG.debug("Request to get BrandMedications by name like='{}' {}", name, pageable);
        return brandMedicationRepository.findByNameContainsIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Page<BrandMedication> findByManufacturer(String manufacturer, Pageable pageable) {
        LOG.debug("Request to get BrandMedications by manufacturer like='{}' {}", manufacturer, pageable);
        return brandMedicationRepository.findByManufacturerContainsIgnoreCase(manufacturer, pageable);
    }

    @Transactional(readOnly = true)
    public Page<BrandMedication> findByDosageForm(String dosageForm, Pageable pageable) {
        LOG.debug("Request to get BrandMedications by dosageForm like='{}' {}", dosageForm, pageable);
        return brandMedicationRepository.findByDosageFormContainsIgnoreCase(dosageForm, pageable);
    }

    @Transactional(readOnly = true)
    public Page<BrandMedication> findByUsageInstructions(String usageInstructions, Pageable pageable) {
        LOG.debug("Request to get BrandMedications by usageInstructions like='{}' {}", usageInstructions, pageable);
        return brandMedicationRepository.findByUsageInstructionsContainsIgnoreCase(usageInstructions, pageable);
    }

    @Transactional(readOnly = true)
    public Page<BrandMedication> findByRoa(String roa, Pageable pageable) {
        LOG.debug("Request to get BrandMedications by roa like='{}' {}", roa, pageable);
        return brandMedicationRepository.findByRoaContainsIgnoreCase(roa, pageable);
    }

    @Transactional(readOnly = true)
    public Page<BrandMedication> findByExpiresAfterOpening(Boolean expiresAfterOpening, Pageable pageable) {
        LOG.debug("Request to get BrandMedications by expiresAfterOpening={} {}", expiresAfterOpening, pageable);
        return brandMedicationRepository.findByExpiresAfterOpening(expiresAfterOpening, pageable);
    }

    @Transactional(readOnly = true)
    public Page<BrandMedication> findByUseSinglePatient(Boolean useSinglePatient, Pageable pageable) {
        LOG.debug("Request to get BrandMedications by useSinglePatient={} {}", useSinglePatient, pageable);
        return brandMedicationRepository.findByUseSinglePatient(useSinglePatient, pageable);
    }

    @Transactional(readOnly = true)
    public Page<BrandMedication> findByIsActive(Boolean isActive, Pageable pageable) {
        LOG.debug("Request to get BrandMedications by isActive={} {}", isActive, pageable);
        return brandMedicationRepository.findByIsActive(isActive, pageable);
    }
}
