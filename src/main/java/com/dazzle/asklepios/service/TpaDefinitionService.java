package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Country;
import com.dazzle.asklepios.domain.CountryDistrict;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.TpaDefinition;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import com.dazzle.asklepios.repository.CountryDistrictRepository;
import com.dazzle.asklepios.repository.CountryRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.TpaDefinitionRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.tpadefinition.TpaDefinitionResponseVM;
import com.dazzle.asklepios.web.rest.vm.tpadefinition.TpaDefinitionSaveVM;
import com.dazzle.asklepios.web.rest.vm.tpadefinition.TpaDefinitionUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class TpaDefinitionService {

    private static final Logger LOG = LoggerFactory.getLogger(TpaDefinitionService.class);
    private static final String ENTITY_NAME = "tpaDefinition";

    private final TpaDefinitionRepository tpaDefinitionRepository;
    private final NphiesPayerRepository nphiesPayerRepository;
    private final CountryRepository countryRepository;
    private final CountryDistrictRepository countryDistrictRepository;

    public TpaDefinitionService(
            TpaDefinitionRepository tpaDefinitionRepository,
            NphiesPayerRepository nphiesPayerRepository,
            CountryRepository countryRepository,
            CountryDistrictRepository countryDistrictRepository
    ) {
        this.tpaDefinitionRepository = tpaDefinitionRepository;
        this.nphiesPayerRepository = nphiesPayerRepository;
        this.countryRepository = countryRepository;
        this.countryDistrictRepository = countryDistrictRepository;
    }

    public TpaDefinition create(TpaDefinitionSaveVM vm) {
        LOG.debug("Create TPA Definition payload={}", vm);

        if (tpaDefinitionRepository.existsByTpaCodeIgnoreCase(vm.tpaCode())) {
            throw new BadRequestAlertException(
                    "tpaCodeExists",
                    ENTITY_NAME,
                    "TPA Code already exists."
            );
        }

        validateCityRequiresCountry(vm.countryId(), vm.cityId());
        Country country = resolveCountry(vm.countryId());

        TpaDefinition tpa = TpaDefinition.builder()
                .tpaCode(trim(vm.tpaCode()))
                .name(trim(vm.name()))
                .guarantorType(vm.guarantorType() != null ? vm.guarantorType() : GuarantorType.TPA)
                .activationDate(vm.activationDate())
                .isActive(vm.isActive() != null ? vm.isActive() : Boolean.TRUE)
                .taxRegistrationNo(blankToNull(vm.taxRegistrationNo()))
                .country(country)
                .city(resolveCity(vm.cityId(), country))
                .address(blankToNull(vm.address()))
                .phone(blankToNull(vm.phone()))
                .email(blankToNull(vm.email()))
                .insuranceCompanies(new HashSet<>())
                .build();

        tpa.setInsuranceCompanies(resolveInsuranceCompanies(vm.insuranceCompanyIds(), tpa.getIsActive(), Set.of()));
        return tpaDefinitionRepository.save(tpa);
    }

    public TpaDefinition update(TpaDefinitionUpdateVM vm) {
        LOG.debug("Update TPA Definition payload={}", vm);

        TpaDefinition existing = tpaDefinitionRepository.findById(vm.id())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notFound",
                        ENTITY_NAME,
                        "TPA not found."
                ));

        if (!existing.getTpaCode().equalsIgnoreCase(vm.tpaCode())
                && tpaDefinitionRepository.existsByTpaCodeIgnoreCaseAndIdNot(vm.tpaCode(), vm.id())) {
            throw new BadRequestAlertException(
                    "tpaCodeExists",
                    ENTITY_NAME,
                    "TPA Code already exists."
            );
        }

        Boolean nextActive = vm.isActive() != null ? vm.isActive() : existing.getIsActive();
        validateDeactivation(existing, nextActive);
        validateCityRequiresCountry(vm.countryId(), vm.cityId());

        Country country = resolveCountry(vm.countryId());
        existing.setTpaCode(trim(vm.tpaCode()));
        existing.setName(trim(vm.name()));
        existing.setGuarantorType(vm.guarantorType() != null ? vm.guarantorType() : GuarantorType.TPA);
        existing.setActivationDate(vm.activationDate());
        existing.setIsActive(nextActive);
        existing.setTaxRegistrationNo(blankToNull(vm.taxRegistrationNo()));
        existing.setCountry(country);
        existing.setCity(resolveCity(vm.cityId(), country));
        existing.setAddress(blankToNull(vm.address()));
        existing.setPhone(blankToNull(vm.phone()));
        existing.setEmail(blankToNull(vm.email()));

        Set<Long> alreadyLinkedIds = existing.getInsuranceCompanies() == null
                ? Set.of()
                : existing.getInsuranceCompanies().stream().map(NphiesPayer::getId).collect(Collectors.toSet());
        existing.setInsuranceCompanies(resolveInsuranceCompanies(vm.insuranceCompanyIds(), nextActive, alreadyLinkedIds));

        return tpaDefinitionRepository.save(existing);
    }

    public Optional<TpaDefinition> toggleIsActive(Long id) {
        LOG.debug("Toggle TPA isActive id={}", id);
        return tpaDefinitionRepository.findById(id).map(tpa -> {
            boolean nextActive = !Boolean.TRUE.equals(tpa.getIsActive());
            validateDeactivation(tpa, nextActive);
            tpa.setIsActive(nextActive);
            return tpaDefinitionRepository.save(tpa);
        });
    }

    @Transactional(readOnly = true)
    public Optional<TpaDefinition> findOne(Long id) {
        return tpaDefinitionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<TpaDefinitionResponseVM> findAll(Pageable pageable) {
        return toResponsePage(tpaDefinitionRepository.findAll(pageable));
    }

    @Transactional(readOnly = true)
    public Page<TpaDefinitionResponseVM> findActive(Pageable pageable) {
        return toResponsePage(tpaDefinitionRepository.findByIsActiveTrue(pageable));
    }

    @Transactional(readOnly = true)
    public Page<TpaDefinitionResponseVM> findByTpaCode(String tpaCode, Pageable pageable) {
        return toResponsePage(tpaDefinitionRepository.findByTpaCodeContainingIgnoreCase(tpaCode, pageable));
    }

    @Transactional(readOnly = true)
    public Page<TpaDefinitionResponseVM> findByName(String name, Pageable pageable) {
        return toResponsePage(tpaDefinitionRepository.findByNameContainingIgnoreCase(name, pageable));
    }

    @Transactional(readOnly = true)
    public List<NphiesPayer> findLinkedInsuranceCompanies(Long tpaId) {
        TpaDefinition tpa = tpaDefinitionRepository.findById(tpaId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "notFound",
                        ENTITY_NAME,
                        "TPA not found."
                ));
        return tpa.getInsuranceCompanies() == null
                ? List.of()
                : tpa.getInsuranceCompanies().stream().toList();
    }

    private Page<TpaDefinitionResponseVM> toResponsePage(Page<TpaDefinition> page) {
        List<Long> ids = page.getContent().stream().map(TpaDefinition::getId).toList();
        Map<Long, Long> linkedCounts = ids.isEmpty()
                ? Map.of()
                : tpaDefinitionRepository.countLinkedInsuranceCompaniesByIds(ids).stream()
                        .collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> ((Number) row[1]).longValue()
                        ));

        return page.map(tpa -> TpaDefinitionResponseVM.ofEntity(
                tpa,
                linkedCounts.getOrDefault(tpa.getId(), 0L)
        ));
    }

    private void validateDeactivation(TpaDefinition tpa, Boolean nextActive) {
        if (Boolean.TRUE.equals(tpa.getIsActive()) && Boolean.FALSE.equals(nextActive)) {
            long activeLinked = tpaDefinitionRepository.countActiveLinkedInsuranceCompanies(tpa.getId());
            if (activeLinked > 0) {
                throw new BadRequestAlertException(
                        "activeInsuranceLinked",
                        ENTITY_NAME,
                        "Cannot deactivate TPA while linked insurance companies are still Active. Deactivate all linked insurance companies first."
                );
            }
        }
    }

    private Set<NphiesPayer> resolveInsuranceCompanies(
            List<Long> insuranceCompanyIds,
            Boolean tpaIsActive,
            Set<Long> alreadyLinkedIds
    ) {
        if (insuranceCompanyIds == null || insuranceCompanyIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Long> distinctIds = insuranceCompanyIds.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        List<NphiesPayer> payers = nphiesPayerRepository.findAllById(distinctIds);
        if (payers.size() != distinctIds.size()) {
            throw new BadRequestAlertException(
                    "insuranceCompanyNotFound",
                    ENTITY_NAME,
                    "One or more insurance companies were not found."
            );
        }

        boolean addingNewLinks = distinctIds.stream().anyMatch(id -> !alreadyLinkedIds.contains(id));
        if (addingNewLinks && !Boolean.TRUE.equals(tpaIsActive)) {
            throw new BadRequestAlertException(
                    "inactiveTpaCannotLink",
                    ENTITY_NAME,
                    "Inactive TPA cannot be linked to insurance companies."
            );
        }

        return new HashSet<>(payers);
    }

    private Country resolveCountry(Long countryId) {
        if (countryId == null) {
            return null;
        }
        return countryRepository.findById(countryId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "countryNotFound",
                        ENTITY_NAME,
                        "Country not found."
                ));
    }

    private CountryDistrict resolveCity(Long cityId, Country country) {
        if (cityId == null) {
            return null;
        }
        if (country == null) {
            throw new BadRequestAlertException(
                    "countryRequiredForCity",
                    ENTITY_NAME,
                    "Country is required when city is selected."
            );
        }
        CountryDistrict city = countryDistrictRepository.findById(cityId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "cityNotFound",
                        ENTITY_NAME,
                        "City not found."
                ));
        Long cityCountryId = city.getCountry() != null ? city.getCountry().getId() : null;
        if (cityCountryId == null || !cityCountryId.equals(country.getId())) {
            throw new BadRequestAlertException(
                    "cityCountryMismatch",
                    ENTITY_NAME,
                    "City does not belong to the selected country."
            );
        }
        return city;
    }

    private void validateCityRequiresCountry(Long countryId, Long cityId) {
        if (cityId != null && countryId == null) {
            throw new BadRequestAlertException(
                    "countryRequiredForCity",
                    ENTITY_NAME,
                    "Country is required when city is selected."
            );
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
