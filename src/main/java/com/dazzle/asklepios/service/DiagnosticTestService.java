package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.enumeration.TestType;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.web.rest.vm.diagnostictest.DiagnosticTestCreateVM;
import com.dazzle.asklepios.web.rest.vm.diagnostictest.DiagnosticTestUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class DiagnosticTestService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestService.class);
    private final DiagnosticTestRepository repository;

    public DiagnosticTestService(DiagnosticTestRepository repository) {
        this.repository = repository;
    }

    public DiagnosticTest create(DiagnosticTestCreateVM vm) {
        LOG.debug("Create DiagnosticTest: {}", vm);
        DiagnosticTest test = DiagnosticTest.builder()
                .type(vm.type())
                .name(vm.name())
                .internalCode(vm.internalCode())
                .ageSpecific(vm.ageSpecific())
                .ageGroupList(vm.ageGroupList())
                .genderSpecific(vm.genderSpecific())
                .gender(vm.gender())
                .specialPopulation(vm.specialPopulation())
                .specialPopulationValues(vm.specialPopulationValues())
                .price(vm.price())
                .currency(vm.currency())
                .specialNotes(vm.specialNotes())
                .isActive(vm.isActive())
                .isProfile(vm.isProfile())
                .appointable(vm.appointable())
                .build();

        return repository.save(test);
    }

    public Optional<DiagnosticTest> update(Long id, DiagnosticTestUpdateVM vm) {
        return repository.findById(id).map(existing -> {
            existing.setType(vm.type());
            existing.setName(vm.name());
            existing.setInternalCode(vm.internalCode());
            existing.setAgeSpecific(vm.ageSpecific());
            existing.setAgeGroupList(vm.ageGroupList());
            existing.setGenderSpecific(vm.genderSpecific());
            existing.setGender(vm.gender());
            existing.setSpecialPopulation(vm.specialPopulation());
            existing.setSpecialPopulationValues(vm.specialPopulationValues());
            existing.setPrice(vm.price());
            existing.setCurrency(vm.currency());
            existing.setSpecialNotes(vm.specialNotes());
            existing.setAppointable(vm.appointable());
            existing.setIsActive(vm.isActive());
            existing.setIsProfile(vm.isProfile());

            return repository.save(existing);
        });
    }


    @Transactional(readOnly = true)
    public Page<DiagnosticTest> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<DiagnosticTest> findByType(TestType type, Pageable pageable) {
        return repository.findByType(type, pageable);
    }

    @Transactional(readOnly = true)
    public Page<DiagnosticTest> findByName(String name, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<DiagnosticTest> findOne(Long id) {
        return repository.findById(id);
    }


    public Optional<DiagnosticTest> toggleIsActive(Long id) {
        return repository.findById(id)
                .map(p -> {
                    p.setIsActive(!Boolean.TRUE.equals(p.getIsActive()));
                    return repository.save(p);
                });
    }

    @Transactional(readOnly = true)
    public Page<DiagnosticTest> findAllActive(Pageable pageable) {
        return repository.findByIsActiveTrue(pageable);
    }

}