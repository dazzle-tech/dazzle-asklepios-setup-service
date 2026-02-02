package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.domain.enumeration.TestType;
import com.dazzle.asklepios.repository.DiagnosticTestProfileRepository;
import com.dazzle.asklepios.service.DiagnosticTestService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.diagnostictest.DiagnosticTestCreateVM;
import com.dazzle.asklepios.web.rest.vm.diagnostictest.DiagnosticTestResponseVM;
import com.dazzle.asklepios.web.rest.vm.diagnostictest.DiagnosticTestUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/setup")
public class DiagnosticTestController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestController.class);

    private final DiagnosticTestService service;
    private final DiagnosticTestProfileRepository profileRepository;

    public DiagnosticTestController(DiagnosticTestService service, DiagnosticTestProfileRepository profileRepository) {
        this.service = service;
        this.profileRepository = profileRepository;
    }

    @PostMapping("/diagnostic-test")
    public ResponseEntity<DiagnosticTestResponseVM> create(@Valid @RequestBody DiagnosticTestCreateVM vm) {
        LOG.debug("REST request to create DiagnosticTest payload={}", vm);

        DiagnosticTest test = service.create(vm);

        // enrich single response too
        DiagnosticTestResponseVM response = enrichWithDefaultProfile(test);

        return ResponseEntity
                .created(URI.create("/api/setup/diagnostic-test/" + test.getId()))
                .body(response);
    }

    @PutMapping("/diagnostic-test/{id}")
    public ResponseEntity<DiagnosticTestResponseVM> update(@Valid@PathVariable Long id, @RequestBody DiagnosticTestUpdateVM vm) {
        LOG.debug("REST request to update DiagnosticTest id={} payload={}", id, vm);

        return service.update(id, vm)
                .map(updated -> ResponseEntity.ok(enrichWithDefaultProfile(updated)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/diagnostic-test")
    public ResponseEntity<List<DiagnosticTestResponseVM>> list(@ParameterObject Pageable pageable) {
        LOG.debug("REST request to list DiagnosticTests page={}", pageable);

        Page<DiagnosticTest> page = service.findAll(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                enrichWithDefaultProfiles(page.getContent()),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/diagnostic-test/active")
    public ResponseEntity<List<DiagnosticTestResponseVM>> getAllActiveTests(@ParameterObject Pageable pageable) {
        LOG.debug("REST request to list active DiagnosticTests page={}", pageable);

        Page<DiagnosticTest> page = service.findAllActive(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                enrichWithDefaultProfiles(page.getContent()),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/diagnostic-test/by-type/{type}")
    public ResponseEntity<List<DiagnosticTestResponseVM>> findByType(
            @PathVariable TestType type,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to find DiagnosticTests by type={} page={}", type, pageable);

        Page<DiagnosticTest> page = service.findByType(type, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                enrichWithDefaultProfiles(page.getContent()),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/diagnostic-test/by-name/{name}")
    public ResponseEntity<List<DiagnosticTestResponseVM>> findByName(
            @PathVariable String name,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to search DiagnosticTests by name='{}' page={}", name, pageable);

        Page<DiagnosticTest> page = service.findByName(name, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                enrichWithDefaultProfiles(page.getContent()),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/diagnostic-test/by-type-and-name")
    public ResponseEntity<List<DiagnosticTestResponseVM>> findByTypeAndName(
            @RequestParam(required = false) TestType type,
            @RequestParam(required = false) String name,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to search DiagnosticTests by type={} and name='{}' page={}", type, name, pageable);

        Page<DiagnosticTest> page = service.findByTypeAndName(type, name, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                enrichWithDefaultProfiles(page.getContent()),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/diagnostic-test/{id}")
    public ResponseEntity<DiagnosticTestResponseVM> get(@PathVariable Long id) {
        LOG.debug("REST request to get DiagnosticTest id={}", id);

        return service.findOne(id)
                .map(this::enrichWithDefaultProfile)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/diagnostic-test/{id}/toggle-active")
    public ResponseEntity<DiagnosticTestResponseVM> togglePractitionerActiveStatus(@PathVariable Long id) {
        LOG.debug("REST toggle Diagnostic Setup isActive id={}", id);

        return service.toggleIsActive(id)
                .map(this::enrichWithDefaultProfile)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/diagnostic-test/active-appointable")
    public ResponseEntity<List<DiagnosticTestResponseVM>> getActiveAppointable(@ParameterObject Pageable pageable) {
        LOG.debug("REST list active appointable DiagnosticTests pageable={}", pageable);

        Page<DiagnosticTest> page = service.findActiveAppointable(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                enrichWithDefaultProfiles(page.getContent()),
                headers,
                HttpStatus.OK
        );
    }
// Add to DiagnosticTestController.java

    @GetMapping("/diagnostic-test/by-ids")
    public ResponseEntity<List<DiagnosticTestResponseVM>> getByIds(
            @RequestParam(name = "ids") List<Long> ids
    ) {
        LOG.debug("REST request to get DiagnosticTests by ids={}", ids);

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<DiagnosticTest> tests = service.findAllByIds(ids);

        // Optional: keep same input order
        var map = tests.stream().collect(Collectors.toMap(DiagnosticTest::getId, t -> t, (a, b) -> a));
        List<DiagnosticTestResponseVM> body = ids.stream()
                .map(map::get)
                .filter(java.util.Objects::nonNull)
                .map(this::enrichWithDefaultProfile)
                .toList();

        return ResponseEntity.ok(body);
    }

    private DiagnosticTestResponseVM enrichWithDefaultProfile(DiagnosticTest test) {
        DiagnosticTestResponseVM vm = DiagnosticTestResponseVM.ofEntity(test);

        if (test.getType() != TestType.LABORATORY) return vm;

        return profileRepository.findFirstByTest_IdAndIsDefaultTrue(test.getId())
                .map(p -> vm.withDefaultProfile(p.getId(), p.getResultUnit(), p.getResultType(), p.getListOfValueId()))
                .orElse(vm);
    }

    private List<DiagnosticTestResponseVM> enrichWithDefaultProfiles(List<DiagnosticTest> tests) {
        if (tests == null || tests.isEmpty()) return List.of();

        List<Long> ids = tests.stream().map(DiagnosticTest::getId).toList();

        Map<Long, DiagnosticTestProfile> defaults = profileRepository
                .findAllByTest_IdInAndIsDefaultTrue(ids)
                .stream()
                .collect(Collectors.toMap(p -> p.getTest().getId(), p -> p, (a, b) -> a));

        return tests.stream()
                .map(t -> {
                    DiagnosticTestResponseVM vm = DiagnosticTestResponseVM.ofEntity(t);

                    if (t.getType() != TestType.LABORATORY) return vm;

                    DiagnosticTestProfile p = defaults.get(t.getId());
                    if (p == null) return vm;

                    return vm.withDefaultProfile(p.getId(), p.getResultUnit(), p.getResultType(), p.getListOfValueId());
                })
                .toList();
    }
}
