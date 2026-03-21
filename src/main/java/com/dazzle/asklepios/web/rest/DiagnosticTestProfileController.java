package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.DiagnosticTestProfile;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
import com.dazzle.asklepios.service.DiagnosticTestProfileService;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.vm.profile.DiagnosticTestProfileCreateVM;
import com.dazzle.asklepios.web.rest.vm.profile.DiagnosticTestProfileResponseVM;
import com.dazzle.asklepios.web.rest.vm.profile.DiagnosticTestProfileUpdateVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/setup")
public class DiagnosticTestProfileController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticTestProfileController.class);
    private final DiagnosticTestProfileService service;

    public DiagnosticTestProfileController(DiagnosticTestProfileService service) {
        this.service = service;
    }

    // CREATE
    @PostMapping("/diagnostic-test-profiles")
    public ResponseEntity<DiagnosticTestProfileResponseVM> create(
            @Valid @RequestBody DiagnosticTestProfileCreateVM vm) {

        LOG.debug("REST create DiagnosticTestProfile payload={}", vm);
        DiagnosticTestProfile saved = service.create(vm.toEntity());
        return ResponseEntity
                .created(URI.create("/api/setup/diagnostic-test-profiles/" + saved.getId()))
                .body(DiagnosticTestProfileResponseVM.fromEntity(saved));
    }

    // UPDATE
    @PutMapping("/diagnostic-test-profiles/{id}")
    public ResponseEntity<DiagnosticTestProfileResponseVM> update(
            @PathVariable Long id,
            @Valid @RequestBody DiagnosticTestProfileUpdateVM vm) {

        LOG.debug("REST update DiagnosticTestProfile id={} payload={}", id, vm);
        return service.update(id, vm.toEntity())
                .map(DiagnosticTestProfileResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET ALL (Paginated)
    @GetMapping("/diagnostic-test-profiles")
    public ResponseEntity<List<DiagnosticTestProfileResponseVM>> findAll(@ParameterObject Pageable pageable) {
        LOG.debug("REST list DiagnosticTestProfiles page={}", pageable);
        Page<DiagnosticTestProfile> page = service.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(DiagnosticTestProfileResponseVM::fromEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    // GET ALL BY TEST ID (Paginated)
    @GetMapping("/diagnostic-test-profiles/by-test/{testId}")
    public ResponseEntity<List<DiagnosticTestProfileResponseVM>> findAllByTestId(
            @PathVariable Long testId,
            @ParameterObject Pageable pageable) {

        LOG.debug("REST list DiagnosticTestProfiles by testId={} page={}", testId, pageable);
        Page<DiagnosticTestProfile> page = service.findAllByTestId(testId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(
                page.getContent().stream().map(DiagnosticTestProfileResponseVM::fromEntity).toList(),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/diagnostic-test-profiles/internal/by-test/{testId}/lab-profile-ids")
    public ResponseEntity<List<Long>> findLabProfileIds(@PathVariable Long testId) {

        LOG.debug("REST get LAB profile ids by testId={}", testId);

        List<DiagnosticTestProfile> profiles = service.findProfilesForLab(testId);

        List<Long> ids = profiles.stream()
                .map(DiagnosticTestProfile::getId)
                .toList();

        return ResponseEntity.ok(ids);
    }

    @GetMapping("/diagnostic-test-profiles/by-test/{testId}/for-lab")
    public ResponseEntity<List<DiagnosticTestProfileResponseVM>> findProfilesForLab(@PathVariable Long testId) {

        LOG.debug("REST list DiagnosticTestProfiles for LAB by testId={}", testId);


        List<DiagnosticTestProfile> profiles = service.findProfilesForLab(testId);


        List<DiagnosticTestProfileResponseVM> body = profiles.stream()
                .map(DiagnosticTestProfileResponseVM::fromEntity)
                .toList();

        return ResponseEntity.ok(body);
    }


    @PostMapping("/diagnostic-test-profiles/by-test-ids/for-lab")
    public ResponseEntity<Map<Long, List<DiagnosticTestProfile>>> getActiveLabProfilesByTestIds(
            @RequestBody Collection<Long> testIds
    ) {

        LOG.debug("REST request to get active lab profiles by testIds:{}", testIds);

        Map<Long, List<DiagnosticTestProfile>> result = service.findActiveProfilesForLabByTestIds(testIds);

        int keys = (result == null) ? 0 : result.size();
        int totalProfiles = (result == null) ? 0 : result.values().stream().mapToInt(list -> list == null ? 0 : list.size()).sum();

        LOG.debug("REST response active lab profiles by testIds: keys={}, totalProfiles={}", keys, totalProfiles);

        return ResponseEntity.ok(result);
    }


    @DeleteMapping("/diagnostic-test-profiles/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete DiagnosticTestProfile id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/diagnostic-test-profiles/by-test/{testId}")
    public ResponseEntity<Void> deleteAllByTestId(@PathVariable Long testId) {
        LOG.debug("REST delete all DiagnosticTestProfiles for testId={}", testId);
        service.deleteAllByTestId(testId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/diagnostic-test-profiles/{id}/toggle-active")
    public ResponseEntity<DiagnosticTestProfileResponseVM> toggleDiagnosticProfileActiveStatus(@PathVariable Long id) {
        LOG.debug("REST toggle Diagnostic Profile Setup isActive id={}", id);
        return service.toggleIsActive(id)
                .map(DiagnosticTestProfileResponseVM::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/diagnostic-test-profiles/{id}/result-type")
    public ResponseEntity<TestResultType> getResultTypeById(@PathVariable Long id) {
        return service.getResultTypeById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/diagnostic-test-profiles/by-ids")
    public ResponseEntity<List<DiagnosticTestProfileResponseVM>> getProfilesByIds(
            @RequestBody Collection<Long> ids
    ) {

        LOG.debug("REST request to get DiagnosticTestProfiles by ids={}", ids);

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<DiagnosticTestProfile> profiles = service.findAllByIds(ids);

        List<DiagnosticTestProfileResponseVM> body = profiles.stream()
                .map(DiagnosticTestProfileResponseVM::fromEntity)
                .toList();

        return ResponseEntity.ok(body);
    }

}
