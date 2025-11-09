package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Language;
import com.dazzle.asklepios.service.LanguageService;
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
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/setup/languages")
public class LanguageController {

    private static final Logger LOG = LoggerFactory.getLogger(LanguageController.class);

    private final LanguageService languageService;

    public LanguageController(LanguageService languageService) {
        this.languageService = languageService;
    }

    @PostMapping
    public ResponseEntity<Language> create(@Valid @RequestBody Language vm) {
        LOG.debug("REST request to create LanguageMaster: {}", vm);
        Language saved = languageService.create(vm);
        return ResponseEntity
                .created(URI.create("/api/setup/languages/" + saved.getId()))
                .body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Language> update(@PathVariable Long id,
                                                 @Valid @RequestBody Language vm) {
        LOG.debug("REST request to update LanguageMaster id={} with: {}", id, vm);
        Optional<Language> updated = languageService.update(id, vm);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Language>> findAll() {
        LOG.debug("REST request to get all LanguageMaster");
        return ResponseEntity.ok(languageService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Language> findOne(@PathVariable Long id) {
        LOG.debug("REST request to get LanguageMaster id={}", id);
        return languageService.findOne(id)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Optional convenience endpoint to fetch by natural key:
    @GetMapping("/by-key/{langKey}")
    public ResponseEntity<Language> findByLangKey(@PathVariable String langKey) {
        LOG.debug("REST request to get LanguageMaster by langKey={}", langKey);
        return languageService.findByLangKey(langKey)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST request to delete LanguageMaster id={}", id);
        boolean removed = languageService.delete(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
