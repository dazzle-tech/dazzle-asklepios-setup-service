package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.MobileAppConfig;
import com.dazzle.asklepios.domain.enumeration.MobileAppConfigKey;
import com.dazzle.asklepios.service.MobileAppConfigService;
import com.dazzle.asklepios.service.dto.mobileAppConfig.MobileAppConfigCreateDTO;
import com.dazzle.asklepios.service.dto.mobileAppConfig.MobileAppConfigUpdateDTO;
import com.dazzle.asklepios.web.rest.vm.mobileAppConfig.MobileAppConfigResponseVM;
import com.dazzle.asklepios.web.rest.vm.mobileAppConfig.MobileAppConfigValueVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class MobileAppConfigController {

    private static final Logger LOG = LoggerFactory.getLogger(MobileAppConfigController.class);
    private final MobileAppConfigService service;

    public MobileAppConfigController(MobileAppConfigService service) {
        this.service = service;
    }

    @PostMapping("/mobile-app-config")
    public ResponseEntity<MobileAppConfigResponseVM> create(@Valid @RequestBody MobileAppConfigCreateDTO dto) {
        LOG.debug("REST create MobileAppConfig payload={}", dto);
        MobileAppConfig saved = service.create(dto);
        return ResponseEntity
                .created(URI.create("/api/setup/mobile-app-config/" + saved.getId()))
                .body(toResponse(saved));
    }

    @PostMapping(value = "/mobile-app-config/image", consumes = "multipart/form-data")
    public ResponseEntity<MobileAppConfigResponseVM> createImage(
            @RequestParam MobileAppConfigKey configKey,
            @RequestParam("file") MultipartFile file
    ) {
        LOG.debug("REST create MobileAppConfig image key={}", configKey);
        MobileAppConfig saved = service.createImage(configKey, file);
        return ResponseEntity
                .created(URI.create("/api/setup/mobile-app-config/" + saved.getId()))
                .body(toResponse(saved));
    }

    @PutMapping("/mobile-app-config/{id}")
    public ResponseEntity<MobileAppConfigResponseVM> update(
            @PathVariable Long id,
            @Valid @RequestBody MobileAppConfigUpdateDTO dto
    ) {
        LOG.debug("REST update MobileAppConfig id={} payload={}", id, dto);
        return ResponseEntity.ok(toResponse(service.update(id, dto)));
    }

    @PutMapping(value = "/mobile-app-config/{id}/image", consumes = "multipart/form-data")
    public ResponseEntity<MobileAppConfigResponseVM> updateImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        LOG.debug("REST update MobileAppConfig image id={}", id);
        return ResponseEntity.ok(toResponse(service.updateImage(id, file)));
    }

    @DeleteMapping("/mobile-app-config/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST delete MobileAppConfig id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mobile-app-config")
    public ResponseEntity<List<MobileAppConfigResponseVM>> getAll() {
        LOG.debug("REST list MobileAppConfig");
        List<MobileAppConfigResponseVM> body = service.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(body);
    }

    /**
     * Returns the configured value for each requested key.
     * Authentication is the {@code X-Portal-Secret} header ({@code PATIENT_PORTAL_SECRET}),
     * checked by {@link com.dazzle.asklepios.security.CountryListSecretFilter}, not a bearer token.
     * Example: {@code ?keys=SUPPORT_PHONE&keys=BANNER_IMAGE1}
     */
    @GetMapping("/mobile-app-config/value")
    public ResponseEntity<List<MobileAppConfigValueVM>> getValues(
            @RequestParam("keys") List<MobileAppConfigKey> keys
    ) {
        LOG.debug("REST get MobileAppConfig values for keys={}", keys);
        return ResponseEntity.ok(service.findValues(keys));
    }

    private MobileAppConfigResponseVM toResponse(MobileAppConfig entity) {
        return MobileAppConfigResponseVM.ofEntity(entity, service.resolveValue(entity));
    }
}
