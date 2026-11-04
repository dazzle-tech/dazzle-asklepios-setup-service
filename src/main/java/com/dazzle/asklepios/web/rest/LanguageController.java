package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Language;
import com.dazzle.asklepios.domain.enumeration.Direction;
import com.dazzle.asklepios.service.LanguageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/search/by-name/{langName}")
    public ResponseEntity<List<Language>> findByName(@PathVariable @NotNull String langName) {
        LOG.debug("REST find Languages by langName='{}'", langName);
        return ResponseEntity.ok(languageService.findByName(langName.trim()));
    }

    @GetMapping("/search/by-code/{langKey}")
    public ResponseEntity<List<Language>> findByCode(@PathVariable @NotNull String langKey) {
        LOG.debug("REST find Languages by langKey='{}'", langKey);
        return ResponseEntity.ok(languageService.findByCode(langKey.trim()));
    }

    @GetMapping("/search/by-direction/{direction}")
    public ResponseEntity<List<Language>> findByDirection(@PathVariable @NotNull Direction direction) {
        LOG.debug("REST find Languages by direction={}", direction);
        return ResponseEntity.ok(languageService.findByDirection(direction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST request to delete LanguageMaster id={}", id);
        boolean removed = languageService.delete(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
