package com.dazzle.asklepios.web.rest;
import com.dazzle.asklepios.security.SecurityUtils;
import com.dazzle.asklepios.service.DuplicationCandidateService;
import com.dazzle.asklepios.service.FacilityService;
import com.dazzle.asklepios.web.rest.vm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/api/setup/duplication-candidates")
public class DuplicationCandidateController {
    private static final Logger LOG = LoggerFactory.getLogger(DuplicationCandidateController.class);
    private final DuplicationCandidateService service;
    private final FacilityService facilityService;
    public DuplicationCandidateController(DuplicationCandidateService service, FacilityService facilityService) { this.service = service;
        this.facilityService = facilityService;
    }
    @PostMapping
    public ResponseEntity<DuplicationCandidateResponseVM> create( @RequestBody DuplicationCandidateCreateVM vm)
    { LOG.debug("REST  request to create DuplicationCandidate : {}", vm);
        String user = SecurityUtils.getCurrentUserLogin().orElse("system");
        DuplicationCandidateResponseVM created = service.create(vm, user);
        return ResponseEntity.ok(created); }


    @PutMapping("/{id}")
    public ResponseEntity<DuplicationCandidateResponseVM> update( @PathVariable Long id, @RequestBody DuplicationCandidateUpdateVM vm)
    { LOG.debug("REST request to update DuplicationCandidate : {}", id);
        String user = SecurityUtils.getCurrentUserLogin().orElse("system");
        Optional<DuplicationCandidateResponseVM> updated = service.update(id, vm,user);
        return updated.map(ResponseEntity::ok) .orElseGet(() -> ResponseEntity.notFound().build()); }





    @GetMapping
    public List<DuplicationCandidateResponseVM> findAll(@RequestParam(required = false) String role) {
        if (role != null && !role.trim().isEmpty()) {
            return service.findByRoleFilter(role.trim());
        } else {
            return service.findAll();
        }
    }

    @PutMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id)
    { LOG.debug("REST request to deactivate DuplicationCandidate : {}", id);
        String user = SecurityUtils.getCurrentUserLogin().orElse("system");
        boolean deactivated = service.deactivate(id, user);
        if (!deactivated) { return ResponseEntity.notFound().build(); }
        return ResponseEntity.noContent().build(); }

    @PutMapping("/reactivate/{id}")
    public ResponseEntity<Void> reactivate(@PathVariable Long id)
    { LOG.debug("REST request to deactivate DuplicationCandidate : {}", id);
        String user = SecurityUtils.getCurrentUserLogin().orElse("system");
        boolean deactivated = service.reactivate(id, user);
        if (!deactivated) { return ResponseEntity.notFound().build(); }
        return ResponseEntity.noContent().build(); }


    @GetMapping("/available-for-role/{roleId}")
    public List<FacilityResponseVM> getAvailableForRole(@PathVariable Long roleId) {
        return facilityService.findUnlinkedOrLinkedToRole(roleId);
    }


}