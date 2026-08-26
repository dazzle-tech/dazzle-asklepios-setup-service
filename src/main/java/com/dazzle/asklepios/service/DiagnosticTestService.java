package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
import com.dazzle.asklepios.domain.enumeration.TestType;
import com.dazzle.asklepios.repository.DiagnosticTestProfileRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.diagnostictest.DiagnosticTestCreateVM;
import com.dazzle.asklepios.web.rest.vm.diagnostictest.DiagnosticTestUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DiagnosticTestService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestService.class);

    private final DiagnosticTestRepository repository;
    private final DiagnosticTestProfileRepository profileRepository;
    private final BillingRuleReferenceService billingRuleReferenceService;

    public DiagnosticTestService(
            DiagnosticTestRepository repository,
            DiagnosticTestProfileRepository profileRepository,
            BillingRuleReferenceService billingRuleReferenceService
    ) {
        this.repository = repository;
        this.profileRepository = profileRepository;
        this.billingRuleReferenceService = billingRuleReferenceService;
    }
    private void validateDefaultProfileFields(
            TestResultType resultType,
            String listOfValueId
    ) {

        if (resultType == TestResultType.LOV && listOfValueId == null) {
            throw new BadRequestAlertException(
                    "listOfValueId is required for LOV result type",
                    "diagnosticTest",
                    "lovmissing"
            );
        }

        if (resultType == TestResultType.TEXT && listOfValueId != null) {
            throw new BadRequestAlertException(
                    "listOfValueId is not allowed for TEXT result type",
                    "diagnosticTest",
                    "invalidlov"
            );
        }
    }
    public DiagnosticTest create(DiagnosticTestCreateVM vm) {
        LOG.debug("Create DiagnosticTest: {}", vm);

        DiagnosticTest test = DiagnosticTest.builder()
                .type(vm.type())
                .name(vm.name())
                .shortName(vm.shortName())
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
                .isActive(vm.isActive() != null ? vm.isActive() : true)
                .appointable(vm.appointable())
                .parallelCapacityValue(vm.parallelCapacityValue() != null ? vm.parallelCapacityValue() : 1)
                .defaultDurationMinutes(vm.defaultDurationMinutes())
                .defaultBufferBeforeMinutes(vm.defaultBufferBeforeMinutes() != null ? vm.defaultBufferBeforeMinutes() : 0)
                .defaultBufferAfterMinutes(vm.defaultBufferAfterMinutes() != null ? vm.defaultBufferAfterMinutes() : 0)
                .modality(vm.modality())
                .billingRule(
                        billingRuleReferenceService.resolveOptional(
                                vm.billingRuleId(),
                                billingRuleReferenceService.toBillingItemType(vm.type())
                        )
                )
                .build();

        validateAppointableRequirements(test);

        DiagnosticTest saved = repository.save(test);

        if (saved.getType() == TestType.LABORATORY) {

            if (vm.defaultProfileResultType() == null) {

                throw new BadRequestAlertException(
                        "defaultProfileResultType is required for LABORATORY tests",
                        "diagnosticTest",
                        "missing_default_profile_result_type"
                );
            }
            validateDefaultProfileFields(
                    vm.defaultProfileResultType(),
                    vm.listOfValueId()
            );
            DiagnosticTestProfile defaultProfile = DiagnosticTestProfile.builder()
                    .test(saved)
                    .name(saved.getName())
                    .resultUnit(vm.defaultProfileResultUnit())
                    .resultType(vm.defaultProfileResultType())
                    .listOfValueId(vm.listOfValueId())
                    .isDefault(true)
                    .isActive(true)
                    .build();

            profileRepository.save(defaultProfile);
        }

        return saved;
    }

    public Optional<DiagnosticTest> update(Long id, DiagnosticTestUpdateVM vm) {
        return repository.findById(id).map(existing -> {

            existing.setType(vm.type());
            existing.setName(vm.name());
            existing.setShortName(vm.shortName());
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

            if (vm.parallelCapacityValue() != null) {
                existing.setParallelCapacityValue(vm.parallelCapacityValue());
            }
            if (vm.defaultDurationMinutes() != null) {
                existing.setDefaultDurationMinutes(vm.defaultDurationMinutes());
            }
            if (vm.defaultBufferBeforeMinutes() != null) {
                existing.setDefaultBufferBeforeMinutes(vm.defaultBufferBeforeMinutes());
            }
            if (vm.defaultBufferAfterMinutes() != null) {
                existing.setDefaultBufferAfterMinutes(vm.defaultBufferAfterMinutes());
            }

            existing.setModality(vm.modality());
            existing.setBillingRule(
                    billingRuleReferenceService.resolveOptional(
                            vm.billingRuleId(),
                            billingRuleReferenceService.toBillingItemType(vm.type())
                    )
            );
            validateAppointableRequirements(existing);

            DiagnosticTest saved = repository.save(existing);

            if (saved.getType() == TestType.LABORATORY) {
                if (vm.defaultProfileResultType() != null) {
                    validateDefaultProfileFields(
                            vm.defaultProfileResultType(),
                            vm.listOfValueId()
                    );
                }
                DiagnosticTestProfile defaultProfile = profileRepository
                        .findFirstByTest_IdAndIsDefaultTrue(saved.getId())
                        .orElseGet(() -> {
                            if (vm.defaultProfileResultType() == null) {
                                throw new BadRequestAlertException(
                                        "defaultProfileResultType is required to create default profile",
                                        "diagnosticTest",
                                        "missing_default_profile_result_type"
                                );
                            }
                            DiagnosticTestProfile created = DiagnosticTestProfile.builder()
                                    .test(saved)
                                    .name(saved.getName())
                                    .resultUnit(vm.defaultProfileResultUnit())
                                    .resultType(vm.defaultProfileResultType())
                                    .listOfValueId(vm.listOfValueId())
                                    .isDefault(true)
                                    .isActive(true)
                                    .build();
                            return profileRepository.save(created);
                        });

                boolean changed = false;

                if (!saved.getName().equals(defaultProfile.getName())) {
                    defaultProfile.setName(saved.getName());
                    changed = true;
                }

                if (vm.defaultProfileResultType() != null && vm.defaultProfileResultType() != defaultProfile.getResultType()) {
                    defaultProfile.setResultType(vm.defaultProfileResultType());
                    changed = true;
                }

                if (vm.defaultProfileResultUnit() != null && !vm.defaultProfileResultUnit().equals(defaultProfile.getResultUnit())) {
                    defaultProfile.setResultUnit(vm.defaultProfileResultUnit());
                    changed = true;
                }

                if (vm.listOfValueId() != null && !vm.listOfValueId().equals(defaultProfile.getListOfValueId())) {
                    defaultProfile.setListOfValueId(vm.listOfValueId());
                    changed = true;
                }

                if (!Boolean.TRUE.equals(defaultProfile.getIsDefault())) {
                    defaultProfile.setIsDefault(true);
                    changed = true;
                }

                if (defaultProfile.getIsActive() == null) {
                    defaultProfile.setIsActive(true);
                    changed = true;
                }

                if (changed) {
                    profileRepository.save(defaultProfile);
                }
            }

            return saved;
        });
    }

    private void validateAppointableRequirements(DiagnosticTest diagnosticTest) {
        if (Boolean.TRUE.equals(diagnosticTest.getAppointable())) {
            if (diagnosticTest.getDefaultDurationMinutes() == null || diagnosticTest.getDefaultDurationMinutes() <= 0) {
                throw new BadRequestAlertException(
                        "defaultDurationMinutes must be greater than 0 when appointable is true",
                        "diagnosticTest",
                        "defaultdurationinvalid"
                );
            }

            if (diagnosticTest.getDefaultBufferBeforeMinutes() == null || diagnosticTest.getDefaultBufferBeforeMinutes() < 0) {
                throw new BadRequestAlertException(
                        "defaultBufferBeforeMinutes must be 0 or greater when appointable is true",
                        "diagnosticTest",
                        "bufferbeforeinvalid"
                );
            }

            if (diagnosticTest.getDefaultBufferAfterMinutes() == null || diagnosticTest.getDefaultBufferAfterMinutes() < 0) {
                throw new BadRequestAlertException(
                        "defaultBufferAfterMinutes must be 0 or greater when appointable is true",
                        "diagnosticTest",
                        "bufferafterinvalid"
                );
            }
        }
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

    public Page<DiagnosticTest> findByTypeAndName(TestType type, String name, Pageable pageable) {
        return repository.findByTypeAndNameContainingIgnoreCase(type, name, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<DiagnosticTest> findOne(Long id) {
        return repository.findById(id);
    }

    public Page<DiagnosticTest> findActiveAppointable(Pageable pageable) {
        LOG.debug("Fetching Active Appointable DiagnosticTest pageable={}", pageable);
        return repository.findByIsActiveTrueAndAppointableTrue(pageable);
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

    @Transactional(readOnly = true)
    public Page<DiagnosticTest> findActiveByType(TestType type, Pageable pageable) {

        LOG.debug("Fetch active DiagnosticTests by type: {}, pageable={}", type, pageable);

        return repository.findByTypeAndIsActiveTrue(type, pageable);
    }

    @Transactional(readOnly = true)
    public List<DiagnosticTest> findAllByIds(List<Long> ids) {
        return repository.findAllById(ids);
    }


    private void validateDiagnosticTest(DiagnosticTest diagnosticTest) {

        if (requiresModality(diagnosticTest.getType())
                && (diagnosticTest.getModality() == null
                || diagnosticTest.getModality().isBlank())) {

            throw new BadRequestAlertException(
                    "Modality is required for this diagnostic test type",
                    "diagnosticTest",
                    "modalityrequired"
            );
        }
    }
    private boolean requiresModality(TestType type) {
        return type == TestType.RADIOLOGY;
    }
}