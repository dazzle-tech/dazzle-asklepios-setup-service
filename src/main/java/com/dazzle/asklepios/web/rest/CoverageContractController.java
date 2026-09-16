package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.enumeration.CoverageClassName;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.service.CoverageContractResolutionService;
import com.dazzle.asklepios.service.CoverageContractService;
import com.dazzle.asklepios.service.CoverageLookupService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageContractResolveRequest;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageContractResolveResponse;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageContractResponseVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageContractSaveVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageContractUpdateVM;
import com.dazzle.asklepios.web.rest.vm.coverage.CoverageLookupItemVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class CoverageContractController {

    private static final Logger LOG = LoggerFactory.getLogger(CoverageContractController.class);

    private final CoverageContractService coverageContractService;
    private final CoverageContractResolutionService coverageContractResolutionService;
    private final CoverageLookupService coverageLookupService;

    public CoverageContractController(
            CoverageContractService coverageContractService,
            CoverageContractResolutionService coverageContractResolutionService,
            CoverageLookupService coverageLookupService
    ) {
        this.coverageContractService = coverageContractService;
        this.coverageContractResolutionService = coverageContractResolutionService;
        this.coverageLookupService = coverageLookupService;
    }

    @PostMapping("/coverage-contracts")
    public ResponseEntity<CoverageContractResponseVM> create(@Valid @RequestBody CoverageContractSaveVM vm) {
        LOG.debug("REST create coverage contract payload={}", vm);
        CoverageContractResponseVM body = coverageContractService.create(vm);
        return ResponseEntity.created(URI.create("/api/setup/coverage-contracts/" + body.id())).body(body);
    }

    @PutMapping("/coverage-contracts")
    public ResponseEntity<CoverageContractResponseVM> update(@Valid @RequestBody CoverageContractUpdateVM vm) {
        LOG.debug("REST update coverage contract payload={}", vm);
        return ResponseEntity.ok(coverageContractService.update(vm));
    }

    @PostMapping("/coverage-contracts/resolve")
    public ResponseEntity<CoverageContractResolveResponse> resolve(
            @RequestBody CoverageContractResolveRequest request
    ) {
        LOG.debug("REST resolve coverage contract payload={}", request);
        return ResponseEntity.ok(coverageContractResolutionService.resolve(request));
    }

    @GetMapping("/coverage-contracts/{id:\\d+}")
    public ResponseEntity<CoverageContractResponseVM> get(@PathVariable Long id) {
        LOG.debug("REST get coverage contract id={}", id);
        return ResponseEntity.ok(coverageContractService.get(id));
    }

    @PatchMapping("/coverage-contracts/{id:\\d+}/toggle-active")
    public ResponseEntity<CoverageContractResponseVM> toggleActive(@PathVariable Long id) {
        LOG.debug("REST toggle coverage contract id={}", id);
        return ResponseEntity.ok(coverageContractService.toggleActive(id));
    }

    @GetMapping("/coverage-contracts")
    public ResponseEntity<List<CoverageContractResponseVM>> search(
            @RequestParam(required = false) GuarantorType guarantorType,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) CoverageClassName className,
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug(
                "REST search coverage contracts guarantorType={} companyId={} isActive={} className={} search={} pageable={}",
                guarantorType,
                companyId,
                isActive,
                className,
                search,
                pageable
        );
        return paged(coverageContractService.search(guarantorType, companyId, isActive, className, search, pageable));
    }

    @GetMapping("/coverage-contracts/lookups/companies")
    public ResponseEntity<List<CoverageLookupItemVM>> companies(
            @RequestParam GuarantorType guarantorType,
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageLookupService.searchCompanies(guarantorType, search, pageable));
    }

    @GetMapping("/coverage-contracts/lookups/insurance-payers")
    public ResponseEntity<List<CoverageLookupItemVM>> insurancePayers(
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageLookupService.searchInsurancePayers(search, pageable));
    }

    @GetMapping("/coverage-contracts/lookups/tpa-insurance-payers")
    public ResponseEntity<List<CoverageLookupItemVM>> tpaInsurancePayers(
            @RequestParam Long tpaId,
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageLookupService.searchTpaInsurancePayers(tpaId, search, pageable));
    }

    @GetMapping("/coverage-contracts/lookups/price-lists")
    public ResponseEntity<List<CoverageLookupItemVM>> priceLists(
            @RequestParam Long nphiesPayerId,
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageLookupService.searchInsurancePriceLists(nphiesPayerId, search, pageable));
    }

    @GetMapping("/coverage-contracts/lookups/facilities")
    public ResponseEntity<List<CoverageLookupItemVM>> facilities(
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageLookupService.searchFacilities(search, pageable));
    }

    @GetMapping("/coverage-contracts/lookups/departments")
    public ResponseEntity<List<CoverageLookupItemVM>> departments(
            @RequestParam Long facilityId,
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageLookupService.searchDepartments(facilityId, search, pageable));
    }

    @GetMapping("/coverage-contracts/lookups/services")
    public ResponseEntity<List<CoverageLookupItemVM>> services(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ServiceCategory category,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageLookupService.searchServices(search, category, pageable));
    }

    @GetMapping("/coverage-contracts/lookups/diagnoses")
    public ResponseEntity<List<CoverageLookupItemVM>> diagnoses(
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable
    ) {
        return paged(coverageLookupService.searchDiagnoses(search, pageable));
    }

    private <T> ResponseEntity<List<T>> paged(Page<T> page) {
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
