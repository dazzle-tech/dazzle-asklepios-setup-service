package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Catalog;
import com.dazzle.asklepios.domain.CatalogDiagnosticTest;
import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.TestType;
import com.dazzle.asklepios.repository.CatalogDiagnosticTestRepository;
import com.dazzle.asklepios.repository.CatalogRepository;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.web.rest.vm.catalog.CatalogAddTestsVM;
import com.dazzle.asklepios.web.rest.vm.catalog.CatalogCreateVM;
import com.dazzle.asklepios.web.rest.vm.catalog.CatalogTestVM;
import com.dazzle.asklepios.web.rest.vm.catalog.CatalogUpdateVM;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CatalogService {

    private final CatalogRepository catalogRepository;
    private final DepartmentsRepository departmentRepository;
    private final FacilityRepository facilityRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final CatalogDiagnosticTestRepository catalogDiagnosticTestRepository;

    public Catalog create(CatalogCreateVM vm) {

        Department dept = null;
        if (vm.getDepartmentId() != null) {
            dept = departmentRepository.findById(vm.getDepartmentId())
                    .orElse(null);
        }

        Facility facil = null;
        if (vm.getFacilityId() != null) {
            facil = facilityRepository.findById(vm.getFacilityId())
                    .orElse(null);
        }

        Catalog c = Catalog.builder()
                .name(vm.getName())
                .description(vm.getDescription())
                .type(vm.getType())
                .department(dept)
                .facility(facil)
                .build();

        return catalogRepository.save(c);
    }

    public Optional<Catalog> update(Long id, CatalogUpdateVM vm) {
        return catalogRepository.findById(id).map(c -> {
            if (vm.getName() != null) c.setName(vm.getName());
            if (vm.getDescription() != null) {
                c.setDescription(vm.getDescription());

            } else {

                c.setDescription(null);
            }
            if (vm.getType() != null) c.setType(vm.getType());
            if (vm.getDepartmentId() != null) {
                Department dept = departmentRepository.findById(vm.getDepartmentId())
                        .orElseThrow(() -> new EntityNotFoundException("Department not found: " + vm.getDepartmentId()));
                c.setDepartment(dept);
            } else {
                c.setDepartment(null);
            }
            if (vm.getFacilityId() != null) {
                Facility facil = facilityRepository.findById(vm.getFacilityId())
                        .orElseThrow(() -> new EntityNotFoundException("Facility not found: " + vm.getFacilityId()));
                c.setFacility(facil);
            } else {
                c.setFacility(null);
            }
            return catalogRepository.save(c);
        });
    }

    @Transactional(readOnly = true)
    public Page<Catalog> findAll(Pageable pageable) {
        return catalogRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Catalog> findOne(Long id) {
        return catalogRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Catalog> findByDepartment(Long departmentId, Pageable pageable) {
        if (departmentId == null) {
            return catalogRepository.findAll(pageable);
        }
        return catalogRepository.findByDepartment_Id(departmentId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Catalog> findByType(TestType type, Pageable pageable) {
        if (type == null) {
            return catalogRepository.findAll(pageable);
        }
        return catalogRepository.findByType(type, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Catalog> searchByName(String name, Pageable pageable) {
        if (name == null) {
            return catalogRepository.findAll(pageable);
        }
        return catalogRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public void addTests(Long catalogId, CatalogAddTestsVM vm) {
        Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new EntityNotFoundException("Catalog not found: " + catalogId));

        vm.getTestIds().forEach(testId -> {
            if (!catalogDiagnosticTestRepository.existsByCatalog_IdAndTest_Id(catalogId, testId)) {
                DiagnosticTest test = diagnosticTestRepository.findById(testId)
                        .orElseThrow(() -> new EntityNotFoundException("Test not found: " + testId));
                catalogDiagnosticTestRepository.save(
                        CatalogDiagnosticTest.builder().catalog(catalog).test(test).build()
                );
            }
        });
    }

    @Transactional(readOnly = true)
    public Page<CatalogDiagnosticTest> listTests(Long catalogId, Pageable pageable) {
        return catalogDiagnosticTestRepository.findByCatalog_Id(catalogId, pageable);
    }

    public void removeCatalog(Long catalogId) {
        catalogDiagnosticTestRepository.deleteByCatalog_Id(catalogId);
        catalogRepository.deleteById(catalogId);
    }

    public void removeTest(Long catalogId, Long testId) {
        catalogDiagnosticTestRepository.deleteByCatalog_IdAndTest_Id(catalogId, testId);
    }

    public CatalogTestVM getTestsForCatalog(Long catalogId, Pageable pageable) {
        Page<CatalogDiagnosticTest> page =
                catalogDiagnosticTestRepository.findAllByCatalogId(catalogId, pageable);

        return CatalogTestVM.ofPage(page);
    }

}
