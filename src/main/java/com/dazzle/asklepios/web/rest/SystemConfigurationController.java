package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.SystemConfiguration;
import com.dazzle.asklepios.domain.enumeration.SystemConfigKey;
import com.dazzle.asklepios.service.SystemConfigurationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/setup")
@Slf4j
@RequiredArgsConstructor
public class SystemConfigurationController {
    private static final Logger LOG =
            LoggerFactory.getLogger(SystemConfigurationController.class);
    private final SystemConfigurationService service;

    @GetMapping("/system-config")
    public ResponseEntity<Map<SystemConfigKey, String>> getAll() {
        LOG.info("REST request to get all system configurations");

        Map<SystemConfigKey, String> response = service.getAll();

        LOG.info("Returning {} system configurations", response.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/system-config/details")
    public ResponseEntity<List<SystemConfiguration>> getAllDetails() {
        LOG.info("REST request to get all system configuration details");

        List<SystemConfiguration> response = service.getAllDetails();

        LOG.info("Returning {} system configuration records", response.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/system-config/{key}")
    public ResponseEntity<String> getOne(@PathVariable SystemConfigKey key) {
        LOG.info("REST request to get system configuration [{}]", key);

        String value = service.getValue(key);

        LOG.info("System configuration [{}] loaded successfully", key);

        return ResponseEntity.ok(value);
    }

    @PutMapping("/system-config/{key}")
    public ResponseEntity<SystemConfiguration> updateOne(
            @PathVariable SystemConfigKey key,
            @Valid @RequestBody UpdateSystemConfigVM vm
    ) {
        LOG.info(
                "REST request to update system configuration [{}] with value [{}]",
                key,
                vm.value()
        );

        SystemConfiguration response =
                service.updateValue(key, vm.value());

        LOG.info(
                "System configuration [{}] updated successfully",
                key
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/system-config")
    public ResponseEntity<List<SystemConfiguration>> updateMany(
            @RequestBody Map<SystemConfigKey, String> values
    ) {
        LOG.info(
                "REST request to bulk update system configurations. Count={}",
                values.size()
        );

        List<SystemConfiguration> response =
                service.updateMany(values);

        LOG.info(
                "Bulk update completed successfully. Updated {} configurations",
                response.size()
        );

        return ResponseEntity.ok(response);
    }

    public record UpdateSystemConfigVM(String value) {
    }

    @PostMapping(value = "/system-config/logo", consumes = "multipart/form-data")
    public ResponseEntity<SystemConfiguration> uploadLogo(
            @RequestParam("file") MultipartFile file
    ) {
        LOG.info(
                "REST request to upload system logo. FileName={}, Size={} bytes",
                file != null ? file.getOriginalFilename() : null,
                file != null ? file.getSize() : 0
        );

        SystemConfiguration response =
                service.uploadImage(SystemConfigKey.SYSTEM_LOGO, file);

        LOG.info(
                "System logo uploaded successfully. ConfigKey={}",
                SystemConfigKey.SYSTEM_LOGO
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/system-config/favicon", consumes = "multipart/form-data")
    public ResponseEntity<SystemConfiguration> uploadFavicon(
            @RequestParam("file") MultipartFile file
    ) {
        LOG.info(
                "REST request to upload favicon. FileName={}, Size={} bytes",
                file != null ? file.getOriginalFilename() : null,
                file != null ? file.getSize() : 0
        );

        SystemConfiguration response =
                service.uploadImage(SystemConfigKey.FAVICON, file);

        LOG.info(
                "Favicon uploaded successfully. ConfigKey={}",
                SystemConfigKey.FAVICON
        );

        return ResponseEntity.ok(response);
    }
    @PostMapping(value = "/system-config/login-background", consumes = "multipart/form-data")
    public ResponseEntity<SystemConfiguration> uploadLoginBackground(
            @RequestParam("file") MultipartFile file
    ) {
        LOG.info(
                "REST request to upload login background. FileName={}, Size={} bytes",
                file != null ? file.getOriginalFilename() : null,
                file != null ? file.getSize() : 0
        );

        return ResponseEntity.ok(
                service.uploadImage(SystemConfigKey.LOGIN_BACKGROUND, file)
        );
    }

    @PostMapping(value = "/system-config/sidebar-logo", consumes = "multipart/form-data")
    public ResponseEntity<SystemConfiguration> uploadSidebarLogo(
            @RequestParam("file") MultipartFile file
    ) {
        LOG.info(
                "REST request to upload sidebar logo. FileName={}, Size={} bytes",
                file != null ? file.getOriginalFilename() : null,
                file != null ? file.getSize() : 0
        );
        return ResponseEntity.ok(
                service.uploadImage(SystemConfigKey.SIDEBAR_LOGO, file)
        );
    }
    @PostMapping(value = "/system-config/sidebar-logo-dark", consumes = "multipart/form-data")
    public ResponseEntity<SystemConfiguration> uploadSidebarLogoDark(
            @RequestParam("file") MultipartFile file
    ) {
        LOG.info(
                "REST request to upload sidebar logo dark. FileName={}, Size={} bytes",
                file != null ? file.getOriginalFilename() : null,
                file != null ? file.getSize() : 0
        );
        return ResponseEntity.ok(
                service.uploadImage(SystemConfigKey.SIDEBAR_LOGO_DARK, file)
        );
    }
}