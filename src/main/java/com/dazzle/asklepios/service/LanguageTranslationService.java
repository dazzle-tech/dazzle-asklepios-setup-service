package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.LanguageTranslation;
import com.dazzle.asklepios.repository.LanguageTranslationRepository;
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
public class LanguageTranslationService {

    private static final Logger LOG = LoggerFactory.getLogger(LanguageTranslationService.class);

    private final LanguageTranslationRepository translationRepository;

    public LanguageTranslationService(LanguageTranslationRepository translationRepository) {
        this.translationRepository = translationRepository;
    }

    public LanguageTranslation create(LanguageTranslation vm) {
        LOG.debug("Request to create LanguageTranslation : {}", vm);

        translationRepository.findByLangKeyAndTranslationKey(vm.getLangKey(), vm.getTranslationKey())
                .ifPresent(t -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Translation already exists for (" + vm.getLangKey() + ", " + vm.getTranslationKey() + ")"
                    );
                });

        LanguageTranslation entity = new LanguageTranslation();
        entity.setLangKey(vm.getLangKey());
        entity.setTranslationKey(vm.getTranslationKey());
        entity.setOriginalText(vm.getOriginalText());
        entity.setTranslationText(vm.getTranslationText());
        entity.setVerified(Boolean.TRUE.equals(vm.getVerified()));
        entity.setTranslated(Boolean.TRUE.equals(vm.getTranslated()));

        try {
            return translationRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid translation data", ex);
        }
    }

    public Optional<LanguageTranslation> update(Long id, LanguageTranslation vm) {
        LOG.debug("Request to update LanguageTranslation id={} with data: {}", id, vm);

        return translationRepository.findById(id).map(existing -> {
            // langKey & translationKey are identity — don’t change here
            existing.setOriginalText(vm.getOriginalText());
            existing.setTranslationText(vm.getTranslationText());
            if (vm.getVerified() != null) existing.setVerified(vm.getVerified());
            if (vm.getTranslated() != null) existing.setTranslated(vm.getTranslated());
            LanguageTranslation updated = translationRepository.save(existing);
            LOG.debug("LanguageTranslation id={} updated successfully", id);
            return updated;
        });
    }

    @Transactional(readOnly = true)
    public List<LanguageTranslation> findAll() {
        LOG.debug("Request to get all LanguageTranslations");
        return translationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<LanguageTranslation> findOne(Long id) {
        LOG.debug("Request to get LanguageTranslation : {}", id);
        return translationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<LanguageTranslation> findByPair(String langKey, String translationKey) {
        LOG.debug("Request to get LanguageTranslation by pair ({}, {})", langKey, translationKey);
        return translationRepository.findByLangKeyAndTranslationKey(langKey, translationKey);
    }

    @Transactional(readOnly = true)
    public List<LanguageTranslation> findByLangKey(String langKey) {
        return translationRepository.findAllByLangKey(langKey);
    }

    public boolean delete(Long id) {
        LOG.debug("Request to delete LanguageTranslation : {}", id);
        if (!translationRepository.existsById(id)) {
            return false;
        }
        translationRepository.deleteById(id);
        return true;
    }
}
