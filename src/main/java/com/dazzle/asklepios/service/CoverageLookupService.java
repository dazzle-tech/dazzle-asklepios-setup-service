package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.ICDDiagnosis;
import com.dazzle.asklepios.domain.Icd10Code;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.TpaDefinition;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.ICDDiagnosisRepository;
import com.dazzle.asklepios.repository.Icd10Repository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.repository.TpaDefinitionRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageLookupItemVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class CoverageLookupService {

    private static final String ENTITY = "coverageContract";

    private final NphiesPayerRepository nphiesPayerRepository;
    private final TpaDefinitionRepository tpaDefinitionRepository;
    private final PriceListSetupRepository priceListSetupRepository;
    private final FacilityRepository facilityRepository;
    private final DepartmentsRepository departmentsRepository;
    private final ServiceRepository serviceRepository;
    private final Icd10Repository icd10Repository;
    private final ICDDiagnosisRepository icdDiagnosisRepository;

    public CoverageLookupService(
            NphiesPayerRepository nphiesPayerRepository,
            TpaDefinitionRepository tpaDefinitionRepository,
            PriceListSetupRepository priceListSetupRepository,
            FacilityRepository facilityRepository,
            DepartmentsRepository departmentsRepository,
            ServiceRepository serviceRepository,
            Icd10Repository icd10Repository,
            ICDDiagnosisRepository icdDiagnosisRepository
    ) {
        this.nphiesPayerRepository = nphiesPayerRepository;
        this.tpaDefinitionRepository = tpaDefinitionRepository;
        this.priceListSetupRepository = priceListSetupRepository;
        this.facilityRepository = facilityRepository;
        this.departmentsRepository = departmentsRepository;
        this.serviceRepository = serviceRepository;
        this.icd10Repository = icd10Repository;
        this.icdDiagnosisRepository = icdDiagnosisRepository;
    }

    public Page<CoverageLookupItemVM> searchCompanies(GuarantorType guarantorType, String search, Pageable pageable) {
        if (guarantorType == null) {
            throw new BadRequestAlertException("Guarantor type is required.", ENTITY, "guarantorTypeRequired");
        }
        String q = blankToNull(search);
        if (guarantorType == GuarantorType.TPA) {
            Pageable tpaPageable = withAllowedSort(pageable, "name", "name", "tpaCode", "id");
            Page<TpaDefinition> page = q == null
                    ? tpaDefinitionRepository.findByIsActiveTrue(tpaPageable)
                    : tpaDefinitionRepository.findByIsActiveTrueAndNameContainingIgnoreCase(q, tpaPageable);
            return page.map(tpa -> CoverageLookupItemVM.of(tpa.getId(), tpa.getTpaCode(), tpa.getName()));
        }
        return nphiesPayerRepository.searchActive(q, payerPageable(pageable))
                .map(this::toPayerLookup);
    }

    public Page<CoverageLookupItemVM> searchInsurancePayers(String search, Pageable pageable) {
        return nphiesPayerRepository.searchActive(blankToNull(search), payerPageable(pageable))
                .map(this::toPayerLookup);
    }

    public Page<CoverageLookupItemVM> searchTpaInsurancePayers(Long tpaId, String search, Pageable pageable) {
        TpaDefinition tpa = requireActiveTpa(tpaId);
        String q = blankToNull(search);
        List<NphiesPayer> linked = tpa.getInsuranceCompanies() == null
                ? List.of()
                : tpa.getInsuranceCompanies().stream()
                .filter(payer -> Boolean.TRUE.equals(payer.getIsActive()))
                .filter(payer -> matchesPayerSearch(payer, q))
                .sorted(Comparator.comparing(this::payerDisplayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return toPage(linked, pageable).map(this::toPayerLookup);
    }

    public Page<CoverageLookupItemVM> searchInsurancePriceLists(Long nphiesPayerId, String search, Pageable pageable) {
        if (nphiesPayerId == null) {
            throw new BadRequestAlertException("Insurance name is required to load price lists.", ENTITY, "insuranceRequired");
        }
        NphiesPayer payer = requireActivePayer(nphiesPayerId, "Insurance name");
        Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<PriceListSetup> page = priceListSetupRepository.searchByPayerId(
                payer.getId(),
                blankToNull(search),
                unsorted
        );
        return page.map(priceList -> toPriceListLookup(priceList, payer));
    }

    public Collection<Long> resolvePriceListPayerIds(GuarantorType guarantorType, Long companyId) {
        if (guarantorType == null || companyId == null) {
            throw new BadRequestAlertException("Coverage company is required to load price lists.", ENTITY, "companyRequired");
        }
        if (guarantorType == GuarantorType.INSURANCE) {
            requireActivePayer(companyId, "Insurance company");
            return List.of(companyId);
        }
        if (guarantorType == GuarantorType.TPA) {
            TpaDefinition tpa = requireActiveTpa(companyId);
            return tpa.getInsuranceCompanies() == null
                    ? List.of()
                    : tpa.getInsuranceCompanies().stream()
                    .filter(payer -> Boolean.TRUE.equals(payer.getIsActive()))
                    .map(NphiesPayer::getId)
                    .toList();
        }
        throw new BadRequestAlertException("Guarantor type must be Insurance or TPA.", ENTITY, "invalidGuarantorType");
    }

    public boolean isPayerLinkedToTpa(Long tpaId, Long nphiesPayerId) {
        return resolvePriceListPayerIds(GuarantorType.TPA, tpaId).contains(nphiesPayerId);
    }

    public Page<CoverageLookupItemVM> searchFacilities(String search, Pageable pageable) {
        return facilityRepository.searchActive(blankToNull(search), pageable)
                .map(this::toFacilityLookup);
    }

    public Page<CoverageLookupItemVM> searchDepartments(Long facilityId, String search, Pageable pageable) {
        if (facilityId == null) {
            throw new BadRequestAlertException("Facility is required.", ENTITY, "facilityIdRequired");
        }
        String q = blankToNull(search);
        Page<Department> page = q == null
                ? departmentsRepository.findByFacilityIdAndIsActiveTrue(facilityId, pageable)
                : departmentsRepository.findByFacilityIdAndIsActiveTrueAndNameContainingIgnoreCase(facilityId, q, pageable);
        return page.map(this::toDepartmentLookup);
    }

    public Page<CoverageLookupItemVM> searchServices(String search, ServiceCategory category, Pageable pageable) {
        return serviceRepository.searchActive(blankToNull(search), category, pageable)
                .map(this::toServiceLookup);
    }

    public Page<CoverageLookupItemVM> searchDiagnoses(String search, Pageable pageable) {
        String q = search == null ? "" : search.trim();
        return icd10Repository.findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(q, q, pageable)
                .map(this::toDiagnosisLookup);
    }

    public Facility findFacility(Long id) {
        return id == null ? null : facilityRepository.findById(id).orElse(null);
    }

    public Department findDepartment(Long id) {
        return id == null ? null : departmentsRepository.findById(id).orElse(null);
    }

    public ServiceSetup findService(Long id) {
        return id == null ? null : serviceRepository.findById(id).orElse(null);
    }

    public ICDDiagnosis findDiagnosis(Long id) {
        if (id == null) {
            return null;
        }
        return icdDiagnosisRepository.findById(id)
                .or(() -> icdDiagnosisRepository.findByIcdDiagnosisUid(String.valueOf(id)))
                .or(() -> icd10Repository.findById(id)
                        .map(Icd10Code::getCode)
                        .filter(code -> code != null && !code.isBlank())
                        .flatMap(icdDiagnosisRepository::findFirstByIcdCodeIgnoreCase))
                .orElse(null);
    }

    public NphiesPayer requireActivePayer(Long id, String field) {
        NphiesPayer payer = nphiesPayerRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(field + " was not found.", ENTITY, "notFound"));
        if (!Boolean.TRUE.equals(payer.getIsActive())) {
            throw new BadRequestAlertException(field + " must be active.", ENTITY, "inactive");
        }
        return payer;
    }

    public TpaDefinition requireActiveTpa(Long id) {
        TpaDefinition tpa = tpaDefinitionRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("TPA was not found.", ENTITY, "notFound"));
        if (!Boolean.TRUE.equals(tpa.getIsActive())) {
            throw new BadRequestAlertException("TPA must be active.", ENTITY, "inactive");
        }
        return tpa;
    }

    public PriceListSetup requireInsurancePriceList(Long id) {
        PriceListSetup priceList = priceListSetupRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Price list was not found.", ENTITY, "notFound"));
        if (priceList.getPayerId() == null && priceList.getNphiesPayerId() == null) {
            throw new BadRequestAlertException("Price list must be linked to an insurance company.", ENTITY, "invalidType");
        }
        return priceList;
    }

    public Facility requireActiveFacility(Long id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Facility was not found.", ENTITY, "notFound"));
        if (!Boolean.TRUE.equals(facility.getIsActive())) {
            throw new BadRequestAlertException("Facility must be active.", ENTITY, "inactive");
        }
        return facility;
    }

    public Department requireActiveDepartment(Long id, Long facilityId) {
        Department department = departmentsRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Department was not found.", ENTITY, "notFound"));
        if (!Boolean.TRUE.equals(department.getIsActive())) {
            throw new BadRequestAlertException("Department must be active.", ENTITY, "inactive");
        }
        if (facilityId != null && department.getFacility() != null
                && !facilityId.equals(department.getFacility().getId())) {
            throw new BadRequestAlertException("Department does not belong to the selected facility.", ENTITY, "facilityMismatch");
        }
        return department;
    }

    public ServiceSetup requireActiveService(Long id) {
        ServiceSetup service = serviceRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException("Service was not found.", ENTITY, "notFound"));
        if (!Boolean.TRUE.equals(service.getIsActive())) {
            throw new BadRequestAlertException("Service must be active.", ENTITY, "inactive");
        }
        return service;
    }

    public ICDDiagnosis requireDiagnosis(Long id) {
        return requireDiagnosis(id, null);
    }

    public ICDDiagnosis requireDiagnosis(Long id, String code) {
        ICDDiagnosis diagnosis = findDiagnosis(id);
        if (diagnosis == null && code != null && !code.isBlank()) {
            diagnosis = icdDiagnosisRepository.findFirstByIcdCodeIgnoreCase(code.trim()).orElse(null);
        }
        if (diagnosis == null) {
            throw new BadRequestAlertException("Diagnosis was not found.", ENTITY, "diagnosisNotFound");
        }
        return diagnosis;
    }

    public String diagnosisCode(ICDDiagnosis diagnosis) {
        return diagnosis == null ? null : diagnosis.getIcdCode();
    }

    public String diagnosisName(ICDDiagnosis diagnosis) {
        if (diagnosis == null) {
            return null;
        }
        if (diagnosis.getIcdShortDescription() != null && !diagnosis.getIcdShortDescription().isBlank()) {
            return diagnosis.getIcdShortDescription();
        }
        return diagnosis.getIcdFullDescription();
    }

    public String companyName(GuarantorType guarantorType, Long companyId) {
        if (guarantorType == GuarantorType.TPA) {
            return tpaDefinitionRepository.findById(companyId).map(TpaDefinition::getName).orElse(null);
        }
        return nphiesPayerRepository.findById(companyId).map(this::payerDisplayName).orElse(null);
    }

    public String companyCode(GuarantorType guarantorType, Long companyId) {
        if (guarantorType == GuarantorType.TPA) {
            return tpaDefinitionRepository.findById(companyId).map(TpaDefinition::getTpaCode).orElse(null);
        }
        return nphiesPayerRepository.findById(companyId).map(NphiesPayer::getNphiesId).orElse(null);
    }

    public String payerDisplayName(NphiesPayer payer) {
        if (payer.getNameEn() != null && !payer.getNameEn().isBlank()) {
            return payer.getNameEn();
        }
        return payer.getNameAr();
    }

    private CoverageLookupItemVM toPayerLookup(NphiesPayer payer) {
        return new CoverageLookupItemVM(
                payer.getId(),
                payer.getNphiesId(),
                payerDisplayName(payer),
                payer.getNameAr(),
                null,
                null,
                null,
                payer.getIsActive()
        );
    }

    private CoverageLookupItemVM toPriceListLookup(PriceListSetup priceList, NphiesPayer payer) {
        String extra = priceList.getStatus() != null ? priceList.getStatus().name() : null;
        if (priceList.getVersionNumber() != null) {
            extra = extra == null
                    ? "v" + priceList.getVersionNumber()
                    : extra + " / v" + priceList.getVersionNumber();
        }
        return new CoverageLookupItemVM(
                priceList.getId(),
                priceList.getShortName(),
                priceList.getName(),
                extra,
                null,
                priceList.getEffectiveFrom(),
                priceList.getEffectiveTo(),
                priceList.getIsActive(),
                payer == null ? priceList.getNphiesPayerId() : payer.getId(),
                payer == null ? null : payerDisplayName(payer)
        );
    }

    private CoverageLookupItemVM toFacilityLookup(Facility facility) {
        return new CoverageLookupItemVM(
                facility.getId(),
                facility.getCode(),
                facility.getName(),
                null,
                null,
                null,
                null,
                facility.getIsActive()
        );
    }

    private CoverageLookupItemVM toDepartmentLookup(Department department) {
        return new CoverageLookupItemVM(
                department.getId(),
                department.getCode(),
                department.getName(),
                null,
                department.getEncounterType(),
                null,
                null,
                department.getIsActive()
        );
    }

    private CoverageLookupItemVM toServiceLookup(ServiceSetup service) {
        return new CoverageLookupItemVM(
                service.getId(),
                service.getCode(),
                service.getName(),
                service.getCategory() != null ? service.getCategory().name() : null,
                null,
                null,
                null,
                service.getIsActive()
        );
    }

    private CoverageLookupItemVM toDiagnosisLookup(Icd10Code diagnosis) {
        return new CoverageLookupItemVM(
                diagnosis.getId(),
                diagnosis.getCode(),
                diagnosis.getDescription(),
                diagnosis.getVersion(),
                null,
                null,
                null,
                diagnosis.getIsActive()
        );
    }

    private Pageable payerPageable(Pageable pageable) {
        return withAllowedSort(pageable, "nameEn", "nameEn", "nameAr", "nphiesId", "id");
    }

    private Pageable withAllowedSort(Pageable pageable, String fallbackProperty, String... allowedProperties) {
        Set<String> allowed = Set.of(allowedProperties);
        List<Sort.Order> orders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            if ("name".equals(property) && allowed.contains("nameEn")) {
                property = "nameEn";
            }
            if (allowed.contains(property)) {
                orders.add(new Sort.Order(order.getDirection(), property));
            }
        }
        Sort sort = orders.isEmpty()
                ? Sort.by(Sort.Direction.ASC, fallbackProperty)
                : Sort.by(orders);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private boolean matchesPayerSearch(NphiesPayer payer, String search) {
        if (search == null) {
            return true;
        }
        String q = search.toLowerCase(Locale.ROOT);
        return containsIgnoreCase(payer.getNameEn(), q)
                || containsIgnoreCase(payer.getNameAr(), q)
                || containsIgnoreCase(payer.getNphiesId(), q);
    }

    private boolean containsIgnoreCase(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        if (start >= items.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), items.size());
        }
        int end = Math.min(start + pageable.getPageSize(), items.size());
        return new PageImpl<>(
                items.subList(start, end),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
                items.size()
        );
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
