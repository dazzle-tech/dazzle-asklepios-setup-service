package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.LanguageTranslation;
import com.dazzle.asklepios.service.LanguageTranslationService;
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

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/setup/translations")
public class LanguageTranslationController {

    private static final Logger LOG = LoggerFactory.getLogger(LanguageTranslationController.class);

    private final LanguageTranslationService translationService;

    public LanguageTranslationController(LanguageTranslationService translationService) {
        this.translationService = translationService;
    }

    @PostMapping
    public ResponseEntity<LanguageTranslation> create(@Valid @RequestBody LanguageTranslation vm) {
        LOG.debug("REST request to create LanguageTranslation: {}", vm);
        LanguageTranslation saved = translationService.create(vm);
        return ResponseEntity
                .created(URI.create("/api/setup/translations/" + saved.getId()))
                .body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LanguageTranslation> update(@PathVariable Long id,
                                                      @Valid @RequestBody LanguageTranslation vm) {
        LOG.debug("REST request to update LanguageTranslation id={} with: {}", id, vm);
        Optional<LanguageTranslation> updated = translationService.update(id, vm);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<LanguageTranslation>> findAll() {
        LOG.debug("REST request to get all LanguageTranslations");
        return ResponseEntity.ok(translationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LanguageTranslation> findOne(@PathVariable Long id) {
        LOG.debug("REST request to get LanguageTranslation id={}", id);
        return translationService.findOne(id)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Fetch by natural key pair: (langKey, translationKey)
    @GetMapping("/by-pair")
    public ResponseEntity<LanguageTranslation> findByPair(@RequestParam("lang") String langKey,
                                                          @RequestParam("key") String translationKey) {
        LOG.debug("REST request to get LanguageTranslation by pair ({}, {})", langKey, translationKey);
        return translationService.findByPair(langKey, translationKey)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-lang/{langKey}")
    public ResponseEntity<List<LanguageTranslation>> findByLangKey(@PathVariable String langKey) {
        return ResponseEntity.ok(translationService.findByLangKey(langKey));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST request to delete LanguageTranslation id={}", id);
        boolean removed = translationService.delete(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
