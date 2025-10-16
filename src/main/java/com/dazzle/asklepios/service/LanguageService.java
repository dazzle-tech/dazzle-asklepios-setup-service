package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Language;
import com.dazzle.asklepios.repository.LanguageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LanguageService {

    private static final Logger LOG = LoggerFactory.getLogger(LanguageService.class);

    private final LanguageRepository languageRepository;

    public LanguageService(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    public Language create(Language vm) {
        LOG.debug("Request to create LanguageMaster : {}", vm);

        if (languageRepository.existsByLangKey(vm.getLangKey())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "lang_key already exists: " + vm.getLangKey()
            );
        }

        Language entity = new Language();
        entity.setLangKey(vm.getLangKey());
        entity.setLangName(vm.getLangName());
        entity.setDirection(vm.getDirection());
        entity.setDetails(vm.getDetails());

        try {
            return languageRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            // In case DB constraint throws (unique, not null, etc.)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid LanguageMaster data", ex);
        }
    }

    public Optional<Language> update(Long id, Language vm) {
        LOG.debug("Request to update LanguageMaster id={} with data: {}", id, vm);

        return languageRepository.findById(id).map(existing -> {
            // Do NOT change langKey here; treat it as immutable identity
            existing.setLangName(vm.getLangName());
            existing.setDirection(vm.getDirection());
            existing.setDetails(vm.getDetails());
            Language updated = languageRepository.save(existing);
            LOG.debug("LanguageMaster id={} updated successfully", id);
            return updated;
        });
    }

    @Transactional(readOnly = true)
    public List<Language> findAll() {
        LOG.debug("Request to get all LanguageMaster");
        return languageRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Language> findOne(Long id) {
        LOG.debug("Request to get LanguageMaster : {}", id);
        return languageRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Language> findByLangKey(String langKey) {
        LOG.debug("Request to get LanguageMaster by langKey={}", langKey);
        return languageRepository.findByLangKey(langKey);
    }

    public boolean delete(Long id) {
        LOG.debug("Request to delete LanguageMaster : {}", id);
        if (!languageRepository.existsById(id)) {
            return false;
        }
        // NOTE: if translations reference this language via FK, the DB may block deletes unless ON DELETE CASCADE.
        languageRepository.deleteById(id);
        return true;
    }
}
