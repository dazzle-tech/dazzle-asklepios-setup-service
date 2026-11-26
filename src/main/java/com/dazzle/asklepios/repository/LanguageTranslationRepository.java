package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.LanguageTranslation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LanguageTranslationRepository extends JpaRepository<LanguageTranslation, Long> {

    Optional<LanguageTranslation> findByLangKeyAndTranslationKey(String langKey, String translationKey);

    List<LanguageTranslation> findAllByLangKey(String langKey);

    Page<LanguageTranslation> findByLangKeyAndTranslationTextContainingIgnoreCase(
            String langKey,
            String translationText,
            Pageable pageable
    );

    Page<LanguageTranslation> findByLangKeyAndTranslationKeyContainingIgnoreCase(
            String langKey,
            String translationKey,
            Pageable pageable
    );

    Page<LanguageTranslation> findByLangKeyAndVerified(String langKey, Boolean verified, Pageable pageable);

    Page<LanguageTranslation> findByLangKeyAndTranslated(String langKey, Boolean translated, Pageable pageable);

    List<LanguageTranslation> findAllByTranslationKeyContainingIgnoreCase(String partialKey, Pageable pageable);
}
