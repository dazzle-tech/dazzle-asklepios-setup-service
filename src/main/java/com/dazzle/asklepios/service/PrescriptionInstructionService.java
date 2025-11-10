
package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.PrescriptionInstruction;
import com.dazzle.asklepios.domain.enumeration.AgeGroupType;
import com.dazzle.asklepios.domain.enumeration.MedFrequency;
import com.dazzle.asklepios.domain.enumeration.MedRoa;
import com.dazzle.asklepios.domain.enumeration.UOM;
import com.dazzle.asklepios.repository.PrescriptionInstructionRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PrescriptionInstructionService {
    private static final Logger LOG = LoggerFactory.getLogger(PrescriptionInstructionService.class);

    private final PrescriptionInstructionRepository repository;

    public PrescriptionInstruction create(@Valid PrescriptionInstruction body) {
        LOG.debug("Create PrescriptionInstruction: {}", body);
        return repository.save(body);
    }

    public Optional<PrescriptionInstruction> update(Long id, @Valid PrescriptionInstruction body) {
        LOG.debug("Update PrescriptionInstruction id={} payload={}", id, body);
        PrescriptionInstruction e = repository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("PrescriptionInstruction not found " + id,
                        "prescriptionInstruction", "notfound"));

        if (body.getCategory()  != null) e.setCategory(body.getCategory());
        if (body.getDose()      != null) e.setDose(body.getDose());
        if (body.getUnit()      != null) e.setUnit(body.getUnit());
        if (body.getRout()     != null) e.setRout(body.getRout());
        if (body.getFrequency() != null) e.setFrequency(body.getFrequency());

        return Optional.of(repository.save(e));
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionInstruction> findAll(Pageable pageable) {
        LOG.debug("List PrescriptionInstructions page={}", pageable);
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionInstruction> findByCategory(AgeGroupType category, Pageable pageable) {
        LOG.debug("List PrescriptionInstructions by category='{}' page={}", category, pageable);
        return repository.findByCategory(category, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionInstruction> findByUnit(UOM unit, Pageable pageable) {
        LOG.debug("List PrescriptionInstructions by unit='{}' page={}", unit, pageable);
        return repository.findByUnit(unit, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionInstruction> findByRoute(MedRoa route, Pageable pageable) {
        LOG.debug("List PrescriptionInstructions by route='{}' page={}", route, pageable);
        return repository.findByRout(route, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionInstruction> findByFrequency(MedFrequency frequency, Pageable pageable) {
        LOG.debug("List PrescriptionInstructions by frequency='{}' page={}", frequency, pageable);
        return repository.findByFrequency(frequency, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<PrescriptionInstruction> findOne(Long id) {
        LOG.debug("Get PrescriptionInstruction id={}", id);
        return repository.findById(id);
    }

    public void delete(Long id) {
        LOG.debug("Delete PrescriptionInstruction id={}", id);
        repository.deleteById(id);
    }
}
