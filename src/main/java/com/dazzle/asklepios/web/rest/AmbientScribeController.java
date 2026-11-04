package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.AmbientScribeService;
import com.dazzle.asklepios.web.rest.vm.ambientScribe.AmbientScribeAudioRetentionVM;
import com.dazzle.asklepios.web.rest.vm.ambientScribe.AmbientScribeClinicsEnabledVM;
import com.dazzle.asklepios.web.rest.vm.ambientScribe.AmbientScribeSettingVM;
import com.dazzle.asklepios.web.rest.vm.ambientScribe.AmbientScribeUpdateVM;
import java.util.List;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/setup")
public class AmbientScribeController {

    private static final Logger LOG = LoggerFactory.getLogger(AmbientScribeController.class);

    private final AmbientScribeService ambientScribeService;

    public AmbientScribeController(AmbientScribeService ambientScribeService) {
        this.ambientScribeService = ambientScribeService;
    }

    @GetMapping("/ambient-scribe/internalJob")
    public ResponseEntity<List<AmbientScribeAudioRetentionVM>> listAudioRetention() {
        LOG.debug("REST list Ambient Scribe audio retention for the retention job");
        return ResponseEntity.ok(ambientScribeService.listAudioRetention());
    }

    @GetMapping("/ambient-scribe/by-facility/{facilityId}")
    public ResponseEntity<AmbientScribeSettingVM> getByFacility(@PathVariable Long facilityId) {
        LOG.debug("REST get Ambient Scribe setup facilityId={}", facilityId);
        return ResponseEntity.ok(ambientScribeService.getByFacility(facilityId));
    }

    @PutMapping("/ambient-scribe/by-facility/{facilityId}")
    public ResponseEntity<AmbientScribeSettingVM> save(
        @PathVariable Long facilityId,
        @Valid @RequestBody AmbientScribeUpdateVM update
    ) {
        LOG.debug("REST save Ambient Scribe setup facilityId={}", facilityId);
        return ResponseEntity.ok(ambientScribeService.save(facilityId, update));
    }

    @PostMapping("/ambient-scribe/by-facility/{facilityId}/clinics-enabled")
    public ResponseEntity<AmbientScribeSettingVM> setClinicsEnabled(
        @PathVariable Long facilityId,
        @Valid @RequestBody AmbientScribeClinicsEnabledVM body
    ) {
        LOG.debug("REST set Ambient Scribe clinics enabled facilityId={} enabled={}", facilityId, body.enabled());
        return ResponseEntity.ok(ambientScribeService.setClinicsEnabled(facilityId, Boolean.TRUE.equals(body.enabled())));
    }
}
