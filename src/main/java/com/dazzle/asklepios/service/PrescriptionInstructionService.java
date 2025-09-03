package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.PrescriptionInstruction;

import java.util.List;
import java.util.Optional;

import com.dazzle.asklepios.repository.PrescriptionInstructionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PrescriptionInstructionService {

    private static final Logger LOG = LoggerFactory.getLogger(PrescriptionInstructionService.class);

    private final PrescriptionInstructionRepository prescriptionInstructionRepository;

    public PrescriptionInstructionService(PrescriptionInstructionRepository prescriptionInstructionRepository) {
        this.prescriptionInstructionRepository = prescriptionInstructionRepository;
    }

    @CacheEvict(cacheNames = PrescriptionInstructionRepository.PRESCRIPTIONINSTRUCTION, key = "'all'")
    public PrescriptionInstruction create(PrescriptionInstruction prescriptionInstruction) {
        LOG.debug("Request to create Prescription Instruction : {}", prescriptionInstruction);
        prescriptionInstruction.setId(null); // ensure a new entity
        return prescriptionInstructionRepository.save(prescriptionInstruction);
    }

    @CacheEvict(cacheNames = PrescriptionInstructionRepository.PRESCRIPTIONINSTRUCTION, key = "'all'")
    public Optional<PrescriptionInstruction> update(Long id, PrescriptionInstruction prescriptionInstruction) {
        LOG.debug("Request to update Prescription Instruction id={} with : {}", id, prescriptionInstruction);
        return prescriptionInstructionRepository
                .findById(id)
                .map(existing -> {
                    existing.setName(prescriptionInstruction.getName());
                    existing.setDose(prescriptionInstruction.getDose());
                    return prescriptionInstructionRepository.save(existing);
                });
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = PrescriptionInstructionRepository.PRESCRIPTIONINSTRUCTION, key = "'all'")
    public List<PrescriptionInstruction> findAll() {
        LOG.debug("Request to get all Prescription Instruction");
        return prescriptionInstructionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<PrescriptionInstruction> findOne(Long id) {
        LOG.debug("Request to get Prescription Instruction : {}", id);
        return prescriptionInstructionRepository.findById(id);
    }

    @CacheEvict(cacheNames = PrescriptionInstructionRepository.PRESCRIPTIONINSTRUCTION, key = "'all'")
    public void delete(Long id) {
        LOG.debug("Request to delete Prescription Instruction : {}", id);
        prescriptionInstructionRepository.deleteById(id);
    }
}
