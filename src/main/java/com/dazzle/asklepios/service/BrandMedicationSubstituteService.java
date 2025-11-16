package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.domain.BrandMedicationSubstitute;
import com.dazzle.asklepios.repository.BrandMedicationRepository;
import com.dazzle.asklepios.repository.BrandMedicationSubstituteRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.brandMedicationSubstitute.BrandMedicationSubstituteCreateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class BrandMedicationSubstituteService {

    private static final Logger LOG = LoggerFactory.getLogger(BrandMedicationSubstituteService.class);

    private final BrandMedicationSubstituteRepository substituteRepository;
    private final BrandMedicationRepository brandRepository;

    public BrandMedicationSubstituteService(
            BrandMedicationSubstituteRepository substituteRepository,
            BrandMedicationRepository brandRepository
    ) {
        this.substituteRepository = substituteRepository;
        this.brandRepository = brandRepository;
    }

    public BrandMedicationSubstitute create(BrandMedicationSubstituteCreateVM vm) {
        LOG.debug("Request to create BrandMedicationSubstitute : {}", vm);

        if (Objects.equals(vm.brandId(), vm.alternativeBrandId())) {
            throw new BadRequestAlertException(
                    "brandId and alternativeBrandId must be different",
                    "brandMedicationSubstitute",
                    "invalidpair"
            );
        }

        BrandMedication brand = brandRepository.findById(vm.brandId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "BrandMedication not found with id " + vm.brandId(),
                        "brandMedication",
                        "notfound"
                ));

        BrandMedication alternative = brandRepository.findById(vm.alternativeBrandId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "Alternative BrandMedication not found with id " + vm.alternativeBrandId(),
                        "brandMedication",
                        "notfound"
                ));

        // prevent duplicates in either direction
        boolean exists = substituteRepository.findAllByBrandOrAlternative(vm.brandId()).stream()
                .anyMatch(bms ->
                        (bms.getBrandMedication().getId().equals(vm.brandId()) &&
                                bms.getAlternativeBrandMedication().getId().equals(vm.alternativeBrandId()))
                                ||
                                (bms.getBrandMedication().getId().equals(vm.alternativeBrandId()) &&
                                        bms.getAlternativeBrandMedication().getId().equals(vm.brandId()))
                );

        if (exists) {
            throw new BadRequestAlertException(
                    "Substitute relation already exists for the given brand pair",
                    "brandMedicationSubstitute",
                    "duplicate"
            );
        }

        BrandMedicationSubstitute entity = BrandMedicationSubstitute.builder()
                .brandMedication(brand)
                .alternativeBrandMedication(alternative)
                .build();

        BrandMedicationSubstitute created = substituteRepository.save(entity);
        LOG.debug("Created BrandMedicationSubstitute: {}", created);
        return created;
    }

    @Transactional(readOnly = true)
    public List<BrandMedication> findAllByBrandOrAlternative(Long brandMedicationId) {
        LOG.debug("Request to get BrandMedication by brand or alternative brand brandMedicationId={}", brandMedicationId);
        return brandRepository.findBrandMedicationsByBrandOrAlternative(brandMedicationId);
    }

    public void delete(Long id) {
        LOG.debug("Request to delete BrandMedicationSubstitute : {}", id);
        if (!substituteRepository.existsById(id)) {
            throw new BadRequestAlertException(
                    "BrandMedicationSubstitute not found with id " + id,
                    "brandMedicationSubstitute",
                    "notfound"
            );
        }
        substituteRepository.deleteById(id);
    }

    @Transactional
    public int removeSubstituteLink(long brandId, long altBrandId) {
        return substituteRepository.deleteLinkBetween(brandId, altBrandId);
    }

}
