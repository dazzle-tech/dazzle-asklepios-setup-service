package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.enumeration.TestType;
import com.dazzle.asklepios.service.DiagnosticTestService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.diagnostictest.DiagnosticTestCreateVM;
import com.dazzle.asklepios.web.rest.vm.diagnostictest.DiagnosticTestResponseVM;
import com.dazzle.asklepios.web.rest.vm.diagnostictest.DiagnosticTestUpdateVM;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/setup")
public class DiagnosticTestController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestController.class);

    private final DiagnosticTestService service;

    public DiagnosticTestController(DiagnosticTestService service) {
        this.service = service;
    }

    @PostMapping("/diagnostic-test")
    public ResponseEntity<DiagnosticTestResponseVM> create(@Valid @RequestBody DiagnosticTestCreateVM vm) {
        LOG.debug("REST request to create DiagnosticTest payload={}", vm);

        DiagnosticTest test = service.create(vm);
        DiagnosticTestResponseVM response = service.toResponseVM(test);
        LOG.debug("Result for  created DiagnosticTest id={} response={}", test.getId(), response);
        return ResponseEntity
                .created(URI.create("/api/setup/diagnostic-test/" + test.getId()))
                .body(response);
    }

    @PutMapping("/diagnostic-test/{id}")
    public ResponseEntity<DiagnosticTestResponseVM> update(@Valid @PathVariable Long id, @RequestBody DiagnosticTestUpdateVM vm) {
        LOG.debug("REST request to update DiagnosticTest id={} payload={}", id, vm);

        return service.update(id, vm)
                .map(updated -> ResponseEntity.ok(service.toResponseVM(updated)))
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
                service.toResponseVMs(page.getContent()),
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
                service.toResponseVMs(page.getContent()),
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
                service.toResponseVMs(page.getContent()),
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
                service.toResponseVMs(page.getContent()),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/diagnostic-test/by-internal-code/{internalCode}")
    public ResponseEntity<List<DiagnosticTestResponseVM>> findByInternalCode(
            @PathVariable String internalCode,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug(
                "REST request to search DiagnosticTests by internalCode='{}' page={}",
                internalCode,
                pageable
        );

        Page<DiagnosticTest> page = service.findByInternalCode(internalCode, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                service.toResponseVMs(page.getContent()),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/diagnostic-test/by-short-name/{shortName}")
    public ResponseEntity<List<DiagnosticTestResponseVM>> findByShortName(
            @PathVariable String shortName,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug(
                "REST request to search DiagnosticTests by shortName='{}' page={}",
                shortName,
                pageable
        );

        Page<DiagnosticTest> page = service.findByShortName(shortName, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                service.toResponseVMs(page.getContent()),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/diagnostic-test/by-status/{isActive}")
    public ResponseEntity<List<DiagnosticTestResponseVM>> findByStatus(
            @PathVariable Boolean isActive,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug(
                "REST request to search DiagnosticTests by isActive={} page={}",
                isActive,
                pageable
        );

        Page<DiagnosticTest> page = service.findByIsActive(isActive, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                service.toResponseVMs(page.getContent()),
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
                service.toResponseVMs(page.getContent()),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/diagnostic-test/{id}")
    public ResponseEntity<DiagnosticTestResponseVM> get(@PathVariable Long id) {
        LOG.debug("REST request to get DiagnosticTest id={}", id);

        return service.findOne(id)
                .map(service::toResponseVM)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/diagnostic-test/internalJob/{id}")
    public ResponseEntity<DiagnosticTestResponseVM> getInternal(@PathVariable Long id) {
        LOG.debug("REST request to get DiagnosticTest internal id={}", id);

        return service.findOne(id)
                .map(service::toResponseVM)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/diagnostic-test/{id}/toggle-active")
    public ResponseEntity<DiagnosticTestResponseVM> toggleActiveStatus(@PathVariable Long id) {
        LOG.debug("REST toggle Diagnostic Setup isActive id={}", id);

        return service.toggleIsActive(id)
                .map(service::toResponseVM)
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
                service.toResponseVMs(page.getContent()),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/diagnostic-test/active/by-type/{type}")
    public ResponseEntity<List<DiagnosticTestResponseVM>> findActiveByType(
            @PathVariable @NotNull TestType type,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long catalogId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug(
                "REST request to find ACTIVE DiagnosticTests by type={}, name='{}', catalogId={} page={}",
                type,
                name,
                catalogId,
                pageable
        );

        if (type == null) {
            throw new BadRequestAlertException(
                    "typeRequired",
                    "diagnosticTest",
                    "Test type is required"
            );
        }

        Page<DiagnosticTest> page = service.findActiveByType(type, name, catalogId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page
        );

        return new ResponseEntity<>(
                service.toResponseVMs(page.getContent()),
                headers,
                HttpStatus.OK
        );
    }

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
        List<DiagnosticTest> ordered = ids.stream()
                .map(map::get)
                .filter(java.util.Objects::nonNull)
                .toList();

        return ResponseEntity.ok(service.toResponseVMs(ordered));
    }

}
